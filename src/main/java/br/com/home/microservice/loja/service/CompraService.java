package br.com.home.microservice.loja.service;

import java.net.URI;
import java.time.LocalDate;
import java.util.Objects;
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
import br.com.home.microservice.loja.client.TransportadorClient;
import br.com.home.microservice.loja.dto.CompraDTO;
import br.com.home.microservice.loja.dto.InfoEntregaDTO;
import br.com.home.microservice.loja.dto.InfoFornecedorDTO;
import br.com.home.microservice.loja.dto.InfoPedidoDTO;
import br.com.home.microservice.loja.dto.VoucherDTO;
import br.com.home.microservice.loja.model.Compra;
import br.com.home.microservice.loja.model.StatusCompra;
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
	
	@Autowired
	private TransportadorClient transportadorClient;
	
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
		
		Compra compraRealizada = Compra.builder()
				.enderecoDestino(compra.getEndereco().toString())
				.status(StatusCompra.RECEBIDO)
				.build();
		compraRepository.save(compraRealizada);
		compra.setCompraId(compraRealizada.getId());
		
		LOG.info("Buscando informações do fornecedor de {}", compra.getEndereco().getEstado());
		
		InfoFornecedorDTO info = fornecedorClient.getInfoPorEstado(compra.getEndereco().getEstado());
		
		LOG.info("Endereço do fornecedor: {}", info.getEndereco());
		
		LOG.info("Realizando um Pedido...");
		
		InfoPedidoDTO pedido = fornecedorClient.realizarPedido(compra.getItens());
		compraRealizada.setStatus(StatusCompra.PEDIDO_REALIZADO);
		compraRealizada.setPedidoId(pedido.getId());
		compraRealizada.setTempoDePreparo(pedido.getTempoDePreparo());
		compraRepository.save(compraRealizada);
		
		//if(1 == 1) throw new RuntimeException(); CASO ACONTEÇA UM ERRO O FLUXO CAIRÁ NO MÉTODO FALLBACKMETHOD
		
		LOG.info("Realizando a reserva para entrega...");
		InfoEntregaDTO entrega = gerarInfoEntrega(compra, info, pedido);
		VoucherDTO voucher = transportadorClient.reservarEntrega(entrega);
		compraRealizada.setStatus(StatusCompra.RESERVA_ENTREGA_REALIZADA);
		compraRealizada.setDataParaEntrega(voucher.getPrevisaoParaEntrega());
		compraRealizada.setVoucher(voucher.getNumero());
		compraRepository.save(compraRealizada);
		
		return compraRealizada;
	}

	private InfoEntregaDTO gerarInfoEntrega(CompraDTO compra, InfoFornecedorDTO info, InfoPedidoDTO pedido) {
		InfoEntregaDTO entrega = new InfoEntregaDTO();
		entrega.setPedidoId(pedido.getId());
		entrega.setDataParaEntrega(LocalDate.now().plusDays(pedido.getTempoDePreparo()));
		entrega.setEnderecoOrigem(info.getEndereco());
		entrega.setEnderecoDestino(compra.getEndereco().toString());
		return entrega;
	}
	
	public Compra realizaCompraFallback(CompraDTO compra) {
		LOG.error("Não foi possível realizar o pedido");
		
		if(Objects.nonNull(compra.getCompraId())) {
			return compraRepository.findById(compra.getCompraId()).get();
		}
		
		return Compra.builder().status(StatusCompra.NAO_PROCESSADA).build();
	}

}
