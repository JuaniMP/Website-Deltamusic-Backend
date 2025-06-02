package co.edu.unbosque.utils;

import co.edu.unbosque.entity.*;
import co.edu.unbosque.repository.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.pdf.PdfWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReporteService {

    @Autowired
    private VentaRepository ventaRepository;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    // --------- Lógica de estadísticas ----------
    public Map<String, Object> calcularEstadisticasMesActual() {
        Map<String, Object> stats = new LinkedHashMap<>();
        Calendar cal = Calendar.getInstance();
        int mes = cal.get(Calendar.MONTH) + 1;
        int anio = cal.get(Calendar.YEAR);

        // 1. Obtener todas las ventas del mes
        List<Venta> ventasDelMes;
        try {
            ventasDelMes = ventaRepository.findVentasByMes(mes, anio);
        } catch (Exception e) {
            ventasDelMes = new ArrayList<>(); // fallback vacío si tu método personalizado aún no existe
        }

        // Top 3 productos más vendidos
        Map<Long, Integer> productoCantidad = new HashMap<>();
        for (Venta v : ventasDelMes) {
            if (v.getDetalles() != null) {
                for (DetalleVenta d : v.getDetalles()) {
                    productoCantidad.put(
                        (long) d.getIdProducto(),
                        productoCantidad.getOrDefault((long) d.getIdProducto(), 0) + d.getCantComp()
                    );
                }
            }
        }
        List<Map.Entry<Long, Integer>> productosOrdenados = new ArrayList<>(productoCantidad.entrySet());
        productosOrdenados.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        List<String> top3ProductosMasVendidos = new ArrayList<>();
        for (int i = 0; i < Math.min(3, productosOrdenados.size()); i++) {
            Long idProd = productosOrdenados.get(i).getKey();
            Producto p = productoRepository.findById(idProd).orElse(null);
            String nombre = (p != null) ? p.getDescripcion() : "ID " + idProd;
            top3ProductosMasVendidos.add(nombre + " (vendidos: " + productosOrdenados.get(i).getValue() + ")");
        }
        stats.put("Top 3 productos más vendidos", top3ProductosMasVendidos);

     // --- TOP 3 PRODUCTOS MENOS VENDIDOS (primero los de 0 ventas, SIN REPES en top más vendidos) ---
        List<String> top3ProductosMenosVendidos = new ArrayList<>();
        Set<Long> topVendidosIds = productosOrdenados.stream()
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        // Primero, los de 0 ventas y que NO estén en top más vendidos
        Iterable<Producto> todosProductos = productoRepository.findAll();
        List<Producto> ceroVentas = new ArrayList<>();
        for (Producto p : todosProductos) {
            if (!productoCantidad.containsKey(p.getId()) && !topVendidosIds.contains(p.getId())) {
                ceroVentas.add(p);
            }
        }
        int encontrados = 0;
        for (Producto p : ceroVentas) {
            top3ProductosMenosVendidos.add(p.getDescripcion() + " (vendidos: 0)");
            encontrados++;
            if (encontrados == 3) break;
        }
        // Si faltan, completa con los que tienen menor cantidad, excluyendo top más vendidos y ya agregados
        if (encontrados < 3) {
            List<Map.Entry<Long, Integer>> candidatos = productosOrdenados.stream()
                .filter(e -> !topVendidosIds.contains(e.getKey()))
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
                .collect(Collectors.toList());
            for (Map.Entry<Long, Integer> e : candidatos) {
                Producto p = productoRepository.findById(e.getKey()).orElse(null);
                if (p != null) {
                    top3ProductosMenosVendidos.add(p.getDescripcion() + " (vendidos: " + e.getValue() + ")");
                    encontrados++;
                    if (encontrados == 3) break;
                }
            }
        }
        stats.put("Top 3 productos menos vendidos", top3ProductosMenosVendidos);


        // Top 3 usuarios con más compras
        Map<Integer, Integer> usuarioCompras = new HashMap<>();
        for (Venta v : ventasDelMes) {
            usuarioCompras.put(
                v.getIdCliente(),
                usuarioCompras.getOrDefault(v.getIdCliente(), 0) + 1
            );
        }
        List<Map.Entry<Integer, Integer>> usuariosOrdenados = new ArrayList<>(usuarioCompras.entrySet());
        usuariosOrdenados.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        List<String> top3Usuarios = new ArrayList<>();
        for (int i = 0; i < Math.min(3, usuariosOrdenados.size()); i++) {
            Integer idUser = usuariosOrdenados.get(i).getKey();
            Usuario u = usuarioRepository.findById(Long.valueOf(idUser)).orElse(null);
            String nombre = (u != null) ? u.getLoginUsrio() : "ID " + idUser;
            top3Usuarios.add(nombre + " (" + usuariosOrdenados.get(i).getValue() + " compras)");
        }
        stats.put("Top 3 usuarios con más compras", top3Usuarios);

        // Suma total de IVA cobrado
        int sumaIva = ventasDelMes.stream().mapToInt(Venta::getValorIva).sum();
        stats.put("Suma total de IVA cobrado", sumaIva);

        // Suma total de ingresos (venta + iva)
        int sumaIngresos = ventasDelMes.stream().mapToInt(v -> v.getValorVenta() + v.getValorIva()).sum();
        stats.put("Suma total de ingresos", sumaIngresos);

        // Cantidad total de transacciones realizadas
        stats.put("Total transacciones realizadas", ventasDelMes.size());

        // Promedio de valor de venta por transacción
        double promedio = (ventasDelMes.size() > 0) ? (double) sumaIngresos / ventasDelMes.size() : 0;
        stats.put("Promedio por transacción", promedio);

        return stats;
    }

    // ----------- GENERACIÓN DE EXCEL -------------
    public ByteArrayInputStream generarReporteExcel() throws Exception {
        Map<String, Object> estadisticas = calcularEstadisticasMesActual();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Estadísticas");

            // --- ESTILOS ---
            CellStyle titleStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
            titleFont.setFontHeightInPoints((short) 18);
            titleFont.setBold(true);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            CellStyle evenRowStyle = workbook.createCellStyle();
            evenRowStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            evenRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            evenRowStyle.setBorderTop(BorderStyle.THIN);
            evenRowStyle.setBorderBottom(BorderStyle.THIN);
            evenRowStyle.setBorderLeft(BorderStyle.THIN);
            evenRowStyle.setBorderRight(BorderStyle.THIN);

            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
            numberStyle.setBorderTop(BorderStyle.THIN);
            numberStyle.setBorderBottom(BorderStyle.THIN);
            numberStyle.setBorderLeft(BorderStyle.THIN);
            numberStyle.setBorderRight(BorderStyle.THIN);

            CellStyle defaultStyle = workbook.createCellStyle();
            defaultStyle.setBorderTop(BorderStyle.THIN);
            defaultStyle.setBorderBottom(BorderStyle.THIN);
            defaultStyle.setBorderLeft(BorderStyle.THIN);
            defaultStyle.setBorderRight(BorderStyle.THIN);

            // --- TÍTULO ---
            int rowIdx = 0;
            Row titleRow = sheet.createRow(rowIdx++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Estadísticas - Tienda Delta Music");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4)); // título ocupa varias columnas

            rowIdx++; // línea en blanco

            // --- TABLAS DE ESTADÍSTICAS ---
            for (Map.Entry<String, Object> entry : estadisticas.entrySet()) {
                String statName = entry.getKey();

                // Encabezado de cada estadística
                Row headerRow = sheet.createRow(rowIdx++);
                Cell headerCell = headerRow.createCell(0);
                headerCell.setCellValue(statName);
                headerCell.setCellStyle(headerStyle);

                if (entry.getValue() instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> lista = (List<String>) entry.getValue();

                    // Header tabla para listas
                    Row colHeader = sheet.createRow(rowIdx++);
                    Cell cellColHeader = colHeader.createCell(0);
                    cellColHeader.setCellValue("Detalle");
                    cellColHeader.setCellStyle(headerStyle);

                    for (int i = 0; i < lista.size(); i++) {
                        Row row = sheet.createRow(rowIdx++);
                        Cell cell = row.createCell(0);
                        cell.setCellValue(lista.get(i));
                        // Alterna color
                        if (i % 2 == 0) cell.setCellStyle(evenRowStyle);
                        else cell.setCellStyle(defaultStyle);
                    }
                } else {
                    Row row = sheet.createRow(rowIdx++);
                    Cell nameCell = row.createCell(0);
                    nameCell.setCellValue("Valor");
                    nameCell.setCellStyle(headerStyle);

                    Cell valueCell = row.createCell(1);
                    Object valor = entry.getValue();
                    if (valor instanceof Number) {
                        valueCell.setCellValue(((Number) valor).doubleValue());
                        valueCell.setCellStyle(numberStyle);
                    } else {
                        valueCell.setCellValue(valor.toString());
                        valueCell.setCellStyle(defaultStyle);
                    }
                }
                rowIdx++; // Espacio entre secciones
            }

            // Ajusta ancho automático
            for (int i = 0; i < 5; i++) sheet.autoSizeColumn(i);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }


    // ----------- GENERACIÓN DE PDF -------------
    public ByteArrayInputStream generarReportePdf() throws Exception {
        Map<String, Object> estadisticas = calcularEstadisticasMesActual();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, out);
        document.open();

        com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        com.itextpdf.text.Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
        com.itextpdf.text.Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

        Paragraph title = new Paragraph("Reporte Estadístico del Mes Actual\n\n", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        for (Map.Entry<String, Object> entry : estadisticas.entrySet()) {
            document.add(new Paragraph(entry.getKey(), sectionFont));
            if (entry.getValue() instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> lista = (List<String>) entry.getValue();
                for (String v : lista) {
                    document.add(new Paragraph("- " + v, normalFont));
                }
            } else {
                document.add(new Paragraph(entry.getValue().toString(), normalFont));
            }
            document.add(Chunk.NEWLINE);
        }
        document.close();
        return new ByteArrayInputStream(out.toByteArray());
    }
}
