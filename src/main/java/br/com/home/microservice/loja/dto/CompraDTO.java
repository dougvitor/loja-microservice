package br.com.home.microservice.loja.dto;

import java.util.List;

import lombok.Data;

@Data
public class CompraDTO {
	
	private List<ItemCompraDTO> itens;
	
	private EnderecoDTO endereco;

}
