package br.com.home.microservice.loja.model;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
public class Compra {
	
	@Id
	private Long pedidoId;
	
	private Integer tempoDePreparo;
	
	private String enderecoDestino;

}
