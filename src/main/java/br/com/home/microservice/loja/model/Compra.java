package br.com.home.microservice.loja.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Compra {
	
	private Long pedidoId;
	
	private Integer tempoDePreparo;
	
	private String enderecoDestino;

}
