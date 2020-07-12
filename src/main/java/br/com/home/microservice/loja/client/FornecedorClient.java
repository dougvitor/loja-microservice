package br.com.home.microservice.loja.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import br.com.home.microservice.loja.dto.InfoFornecedorDTO;
import br.com.home.microservice.loja.dto.InfoPedidoDTO;
import br.com.home.microservice.loja.dto.ItemCompraDTO;

@FeignClient("fornecedor")
public interface FornecedorClient {

	@RequestMapping("/info/{estado}")
	public InfoFornecedorDTO getInfoPorEstado(@PathVariable String estado);

	@RequestMapping(value = "/pedido", method = RequestMethod.POST)
	InfoPedidoDTO realizarPedido(List<ItemCompraDTO> itens);
}
