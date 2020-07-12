package br.com.home.microservice.loja.service;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import br.com.home.microservice.loja.client.FornecedorClient;
import br.com.home.microservice.loja.dto.CompraDTO;
import br.com.home.microservice.loja.dto.InfoFornecedorDTO;
import br.com.home.microservice.loja.dto.InfoPedidoDTO;
import br.com.home.microservice.loja.model.Compra;

@Service
public class CompraService {
	
	@Autowired
	private RestTemplate restTemplateClient;
	
	@Autowired
	private DiscoveryClient eurekaClient;
	
	@Autowired
	private FornecedorClient fornecedorClient;

	public void realizarCompraRestTemplate(CompraDTO compra) {
		
		eurekaClient
			.getInstances("fornecedor")
				.parallelStream()
					.forEach(fornecedor ->{
						System.out.println(String.format("%s:%d", fornecedor.getHost(), fornecedor.getPort()));
					});
		
		URI uri = URI.create(String.format("http://fornecedor/info/%s", compra.getEndereco().getEstado()));
		
		ResponseEntity<InfoFornecedorDTO> exchange = restTemplateClient.exchange(uri, HttpMethod.GET, null, InfoFornecedorDTO.class);
		
		System.out.println(exchange.getBody().getEndereco());
	}
	
	public Compra realizarCompra(CompraDTO compra) {
		
		InfoFornecedorDTO info = fornecedorClient.getInfoPorEstado(compra.getEndereco().getEstado());
		
		System.out.println(String.format("Endereço do fornecedor: %s", info.getEndereco()));
		
		InfoPedidoDTO pedido = fornecedorClient.realizarPedido(compra.getItens());
		
		Compra compraRealizada = new Compra(pedido.getId(), pedido.getTempoDePreparo(), compra.getEndereco().toString());
		
		return compraRealizada;
	}

}
