package com.jees.webs.abs;

import com.jees.tool.utils.UrlUtil;
import com.jees.webs.support.ISupportEL;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 路径访问控制，需配合WebConfig
 * @author aiyoyoyo
 */
@Log4j2
public abstract class AbsController implements ISupportEL{

    @Autowired
    SessionRegistry  sessionRegistry;
    @Autowired
    AbsTemplateService templateService;
    @Autowired
    AbsSuperService    superService;

    @RequestMapping( "/${jees.webs.logout}" )
    public String logout( HttpServletRequest _request, HttpServletResponse _response ){
        log.debug( "--用户登出" );

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if( auth != null )
            new SecurityContextLogoutHandler().logout( _request, _response, auth );

        sessionRegistry.removeSessionInformation( _request.getSession().getId() );

        return "redirect:/";
    }

    /**
     * 拦截器，只有AbsControllerConfig被继承，且被扫描到后生效。
     *
     * @return
     */
    @Bean
    public HandlerInterceptor handlerInterceptor(){
        log.debug( "--应用AbsControllerConfing拦截器。" );
        return new HandlerInterceptor(){
            @Override
            public boolean preHandle( HttpServletRequest _request, HttpServletResponse _response, Object _handler ) throws IOException{
                String uri = UrlUtil.uri2mapping( _request.getRequestURI() );

                templateService.loadTemplate( uri, _request );
                superService.loadUserMenus( _request );
                superService.loadUserBreadcrumb( _request );
                superService.loadUserActiveMenus( _request );
                return true;
            }
        };
    }
}
