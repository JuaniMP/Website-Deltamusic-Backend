package co.edu.unbosque.controller;

import co.edu.unbosque.utils.ReporteService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@CrossOrigin(origins = "*")
@Slf4j
@RestController
@RequestMapping("/reporte")
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    @GetMapping("/estadisticas/excel")
    public ResponseEntity<Resource> descargarReporteExcel() throws Exception {
        InputStreamResource file = new InputStreamResource(reporteService.generarReporteExcel());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=estadisticas.xlsx")
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(file);
    }

    @GetMapping("/estadisticas/pdf")
    public ResponseEntity<Resource> descargarReportePdf() throws Exception {
        InputStreamResource file = new InputStreamResource(reporteService.generarReportePdf());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=estadisticas.pdf")
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .body(file);
    }
}
