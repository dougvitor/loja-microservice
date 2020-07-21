package br.com.home.microservice.loja.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.home.microservice.loja.dto.CompraDTO;
import br.com.home.microservice.loja.model.Compra;
import br.com.home.microservice.loja.service.CompraService;

@RestController
@RequestMapping("/compra")
public class CompraController {
	
	@Autowired
	private CompraService service;
	
	@GetMapping("/{id}")
	public Compra getById(@PathVariable("id") Long id) {
		Optional<Compra> compra = service.getById(id);
		return compra.orElse(new Compra());
	}
	
	@PostMapping
	public Compra realizarCompra(@RequestBody CompraDTO compra) {
		return service.realizarCompra(compra);
	}

}
