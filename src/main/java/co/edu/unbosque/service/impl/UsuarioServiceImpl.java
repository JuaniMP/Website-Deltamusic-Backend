package co.edu.unbosque.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import co.edu.unbosque.utils.GenericServiceImpl;
import co.edu.unbosque.entity.Usuario;
import co.edu.unbosque.service.api.UsuarioServiceAPI;
import co.edu.unbosque.repository.UsuarioRepository;

import java.util.Optional;

@Service
public class UsuarioServiceImpl extends GenericServiceImpl<Usuario, Long> implements UsuarioServiceAPI {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Override
	public CrudRepository<Usuario, Long> getDao() {
		return usuarioRepository;
	}

	@Override
	public Optional<Usuario> findByCorreoUsuario(String correoUsuario) {
		return usuarioRepository.findByCorreoUsuario(correoUsuario);
	}

}
