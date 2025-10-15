package com.bootcamp.myproject.application.restservice;


import com.bootcamp.myproject.application.model.Parameter;
import com.bootcamp.myproject.application.restrepository.ParameterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ParameterService {

	private final ParameterRepository repo;
	private final Map<String, String> cache = new ConcurrentHashMap<>();

	public Mono<Double> getDouble(String key) {
		return Mono.justOrEmpty(cache.get(key)).switchIfEmpty(repo.findByKey(key).map(Parameter::getValue).doOnNext(v -> cache.put(key, v))).map(Double::parseDouble);
	}
}
