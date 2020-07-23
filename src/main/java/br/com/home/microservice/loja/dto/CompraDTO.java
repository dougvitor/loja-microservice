package br.com.home.microservice.loja.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class CompraDTO {
	
	private List<ItemCompraDTO> itens;
	
	private EnderecoDTO endereco;
	
	@JsonIgnore
	private Long compraId;

}
