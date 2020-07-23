package br.com.home.microservice.loja.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import br.com.home.microservice.loja.dto.InfoEntregaDTO;
import br.com.home.microservice.loja.dto.VoucherDTO;

@FeignClient("transportador")
public interface TransportadorClient {

	@RequestMapping(path = "/entrega", method = RequestMethod.POST)
	public VoucherDTO reservarEntrega(InfoEntregaDTO entregaDto);
	
}
