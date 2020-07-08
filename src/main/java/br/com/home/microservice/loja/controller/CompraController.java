package br.com.home.microservice.loja.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.home.microservice.loja.dto.CompraDTO;
import br.com.home.microservice.loja.service.CompraService;

@RestController
@RequestMapping("/compra")
public class CompraController {
	
	@Autowired
	private CompraService service;
	
	@PostMapping
	public void realizarCompra(@RequestBody CompraDTO compra) {
		service.realizarCompra(compra);
	}

}
