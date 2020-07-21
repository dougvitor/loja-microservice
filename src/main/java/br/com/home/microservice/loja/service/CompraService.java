package br.com.home.microservice.loja.service;

import java.net.URI;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import br.com.home.microservice.loja.client.FornecedorClient;
import br.com.home.microservice.loja.dto.CompraDTO;
import br.com.home.microservice.loja.dto.InfoFornecedorDTO;
import br.com.home.microservice.loja.dto.InfoPedidoDTO;
import br.com.home.microservice.loja.model.Compra;
import br.com.home.microservice.loja.repository.CompraRepository;

@Service
public class CompraService {
	
	private static final Logger LOG = LoggerFactory.getLogger(CompraService.class);
	
	@Autowired
	private RestTemplate restTemplateClient;
	
	@Autowired
	private DiscoveryClient eurekaClient;
	
	@Autowired
	private FornecedorClient fornecedorClient;
	
	@Autowired
	private CompraRepository compraRepository;
	
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
	
	@HystrixCommand(threadPoolKey = "getByIdThreadPool")
	public Optional<Compra> getById(Long id) {
		return compraRepository.findById(id);
	}
	
	@HystrixCommand(fallbackMethod = "realizaCompraFallback", threadPoolKey = "realizarCompraThreadPool")
	public Compra realizarCompra(CompraDTO compra) {
		
		LOG.info("Buscando informações do fornecedor de {}", compra.getEndereco().getEstado());
		
		InfoFornecedorDTO info = fornecedorClient.getInfoPorEstado(compra.getEndereco().getEstado());
		
		LOG.info("Endereço do fornecedor: {}", info.getEndereco());
		
		LOG.info("Realizando um Pedido...");
		
		InfoPedidoDTO pedido = fornecedorClient.realizarPedido(compra.getItens());
		
		Compra compraRealizada = new Compra(pedido.getId(), pedido.getTempoDePreparo(), compra.getEndereco().toString());
		compraRepository.save(compraRealizada);
		
		return compraRealizada;
	}
	
	public Compra realizaCompraFallback(CompraDTO compra) {
		LOG.error("Não foi possível realizar o pedido");
		return null;
	}

}
