package br.com.home.microservice.loja.dto;

import lombok.Data;

@Data
public class EnderecoDTO {
	
	private String rua;
	
	private Integer numero;
	
	private String estado;

}
