package br.com.home.microservice.loja.service;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	
	private static final Logger LOG = LoggerFactory.getLogger(CompraService.class);
	
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
		
		LOG.info("Buscando informações do fornecedor de {}", compra.getEndereco().getEstado());
		
		InfoFornecedorDTO info = fornecedorClient.getInfoPorEstado(compra.getEndereco().getEstado());
		
		LOG.info("Endereço do fornecedor: {}", info.getEndereco());
		
		LOG.info("Realizando um Pedido...");
		
		InfoPedidoDTO pedido = fornecedorClient.realizarPedido(compra.getItens());
		
		Compra compraRealizada = new Compra(pedido.getId(), pedido.getTempoDePreparo(), compra.getEndereco().toString());
		
		return compraRealizada;
	}

}
