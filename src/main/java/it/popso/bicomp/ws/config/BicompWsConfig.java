package it.popso.bicomp.ws.config;

import java.util.List;
import java.util.Properties;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;
import org.springframework.ws.soap.security.wss4j2.callback.SimplePasswordValidationCallbackHandler;
import org.springframework.ws.soap.server.endpoint.interceptor.PayloadRootSmartSoapEndpointInterceptor;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition;
import org.springframework.ws.wsdl.wsdl11.Wsdl11Definition;
import org.springframework.xml.validation.XmlValidator;
import org.springframework.xml.validation.XmlValidatorFactory;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;
import org.springframework.xml.xsd.XsdSchemaCollection;

import it.popso.bicomp.properties.BicompConfig;
import it.popso.bicomp.utils.BicompConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@EnableWs
@Configuration
@RequiredArgsConstructor
public class BicompWsConfig extends WsConfigurerAdapter {
	
	private final BicompConfig config;
	
	
	@Bean
    public SimplePasswordValidationCallbackHandler securityCallbackHandler() {
		
        SimplePasswordValidationCallbackHandler callbackHandler = new SimplePasswordValidationCallbackHandler();
        Properties users = new Properties();
        users.setProperty(this.config.getWs().getContabilitaUsername(), this.config.getWs().getContabilitaPassword());
        users.setProperty(this.config.getWs().getForecastUsername(), this.config.getWs().getForecastPassword());
        callbackHandler.setUsers(users);
        
        return callbackHandler;
    }
	
	@Bean
    public Wss4jSecurityInterceptor securityInterceptor() {
    	
        Wss4jSecurityInterceptor securityInterceptor = new Wss4jSecurityInterceptor();
        securityInterceptor.setValidationActions("UsernameToken");
        securityInterceptor.setValidationCallbackHandler(this.securityCallbackHandler());
        
        return securityInterceptor;
    }
	
	@Override
	public void addInterceptors(List<EndpointInterceptor> interceptors) {
		
		// Security interceptor
		interceptors.add(this.securityInterceptor());
		
		// Request validation interceptor
		BicompValidatingInterceptor validatingInterceptor = new BicompValidatingInterceptor();
		validatingInterceptor.setValidateRequest(true);
        validatingInterceptor.setValidateResponse(false);
        validatingInterceptor.setXsdSchemaCollection(this.schemaCollection());
        interceptors.add(validatingInterceptor);
		
        // register endpoint specific interceptor
        interceptors.add(new PayloadRootSmartSoapEndpointInterceptor(
            new BicompEndpointInterceptor(),
            BicompConstants.LETTURA_NAMESPACE_URI,
            "saldiCgsReadRequest"
        ));
        
        // register endpoint specific interceptor
        interceptors.add(new PayloadRootSmartSoapEndpointInterceptor(
            new BicompEndpointInterceptor(),
            BicompConstants.LETTURA_NAMESPACE_URI,
            "saldiTipsReadRequest"
        ));
        
        // register endpoint specific interceptor
        interceptors.add(new PayloadRootSmartSoapEndpointInterceptor(
            new BicompEndpointInterceptor(),
            BicompConstants.LETTURA_NAMESPACE_URI,
            "ultimoSaldoCgsReadRequest"
        ));
        
        // register endpoint specific interceptor
        interceptors.add(new PayloadRootSmartSoapEndpointInterceptor(
            new BicompEndpointInterceptor(),
            BicompConstants.LETTURA_NAMESPACE_URI,
            "ultimoPagamentoTipsReadRequest"
        ));
        
        // register endpoint specific interceptor
        interceptors.add(new PayloadRootSmartSoapEndpointInterceptor(
            new BicompEndpointInterceptor(),
            BicompConstants.SCRITTURA_NAMESPACE_URI,
            "tipsReportUpdateRequest"
        ));
	}
	
	@Bean
	public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext applicationContext) {
		
		MessageDispatcherServlet servlet = new MessageDispatcherServlet();
		servlet.setApplicationContext(applicationContext);
		
		return new ServletRegistrationBean<>(servlet, "/BicompService_v1/*");
	}

	@Bean(name = "BicompLettura.ws")
	public Wsdl11Definition letturaWsdl11Definition() {
		
		SimpleWsdl11Definition wsdl11Definition = new SimpleWsdl11Definition();
		wsdl11Definition.setWsdl(new ClassPathResource("wsdl/BicompServiceLettura_v1.0.wsdl"));
		
		return wsdl11Definition;
	}
	
	@Bean(name = "BicompScrittura.ws")
	public Wsdl11Definition scritturaWsdl11Definition() {
		
		SimpleWsdl11Definition wsdl11Definition = new SimpleWsdl11Definition();
		wsdl11Definition.setWsdl(new ClassPathResource("wsdl/BicompServiceScrittura_v1.0.wsdl"));
		
		return wsdl11Definition;
	}
	
	@Bean(name = "BicompServiceLetturaSchema_v1.0")
	public XsdSchema bicompServiceLetturaSchema() {
		return new SimpleXsdSchema(new ClassPathResource("wsdl/schema/BicompServiceLetturaSchema_v1.0.xsd"));
	}
	
	@Bean(name = "BicompServiceScritturaSchema_v1.0")
	public XsdSchema bicompServiceScritturaSchema() {
		return new SimpleXsdSchema(new ClassPathResource("wsdl/schema/BicompServiceScritturaSchema_v1.0.xsd"));
	}
	
	@Bean(name = "BicompServiceSchema_v1.0")
	public XsdSchema bicompServiceSchema() {
		return new SimpleXsdSchema(new ClassPathResource("wsdl/schema/BicompServiceSchema_v1.0.xsd"));
	}
	
	@Bean(name = "TipiTestataSchema_v11.0")
	public XsdSchema tipiTestataSchema() {
		return new SimpleXsdSchema(new ClassPathResource("wsdl/schema/TipiTestata_v11.0/TipiTestataSchema_v11.0.xsd"));
	}
	
	@Bean(name = "TestataSchema_v11.0")
	public XsdSchema testataSchema() {
		return new SimpleXsdSchema(new ClassPathResource("wsdl/schema/TipiTestata_v11.0/TestataSchema_v11.0.xsd"));
	}
	
	@Bean(name = "FaultSchema_v11.0")
	public XsdSchema faultSchema() {
		return new SimpleXsdSchema(new ClassPathResource("wsdl/schema/TipiTestata_v11.0/fault/FaultSchema_v11.0.xsd"));
	}
	
	@Bean
	public XsdSchemaCollection schemaCollection() {
		
		return new XsdSchemaCollection() {
        	
            @Override
            public XsdSchema[] getXsdSchemas() {
                return new XsdSchema[] {};
            }

            @Override
            public XmlValidator createValidator() {
                try {
                    return XmlValidatorFactory.createValidator(getSchemas(), "http://www.w3.org/2001/XMLSchema");
                } 
                catch (Exception e) {
                    log.error("Failed to create validator: [{}]", e);
                }
                
                return null;
            }

            public Resource[] getSchemas() {
        		
            	return new Resource[] {
            		new ClassPathResource("wsdl/schema/BicompServiceLetturaSchema_v1.0.xsd"),
            		new ClassPathResource("wsdl/schema/BicompServiceScritturaSchema_v1.0.xsd"),
            		new ClassPathResource("wsdl/schema/TipiTestata_v11.0/fault/FaultSchema_v11.0.xsd")
            	};
        	}
        };
	}

}
