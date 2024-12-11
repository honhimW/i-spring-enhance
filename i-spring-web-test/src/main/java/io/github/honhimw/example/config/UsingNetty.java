package io.github.honhimw.example.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.embedded.netty.NettyRouteProvider;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.boot.web.reactive.server.ReactiveWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ReactorResourceFactory;
import reactor.netty.http.server.HttpServer;

/**
 * @author hon_him
 * @since 2022-10-19
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingBean(ReactiveWebServerFactory.class)
@ConditionalOnClass({HttpServer.class})
public class UsingNetty {

    @Bean
    ReactorResourceFactory reactorServerResourceFactory() {
        return new ReactorResourceFactory();
    }

    @Bean
    NettyReactiveWebServerFactory nettyReactiveWebServerFactory(
        ReactorResourceFactory resourceFactory,
        ObjectProvider<NettyRouteProvider> routes,
        ObjectProvider<NettyServerCustomizer> serverCustomizers) {
        NettyReactiveWebServerFactory serverFactory = new NettyReactiveWebServerFactory();
        serverFactory.setResourceFactory(resourceFactory);
        routes.orderedStream().forEach(serverFactory::addRouteProviders);
        serverFactory.getServerCustomizers().addAll(serverCustomizers.orderedStream().toList());
        return serverFactory;
    }

}
