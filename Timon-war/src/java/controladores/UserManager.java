/*
 *   __    __ _ _     _   ___           _   _     _             __  __
 *  / / /\ \ (_) | __(_) / _ \__ _ _ __| |_(_) __| | ___   /\/\ \ \/ /
 *  \ \/  \/ / | |/ /| |/ /_)/ _` | '__| __| |/ _` |/ _ \ /    \ \  / 
 *   \  /\  /| |   < | / ___/ (_| | |  | |_| | (_| | (_) / /\/\ \/  \ 
 *    \/  \/ |_|_|\_\|_\/    \__,_|_|   \__|_|\__,_|\___/\/    \/_/\_\
 *                                              
 *                                              
 *  
 * Wikipartido de Mexico
 * VER ARCHIVO DE LiCENCIA
 * 
 *
 */
package controladores;

import entities.registro.Miembro;
import java.io.Serializable;
import javax.annotation.Resource;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import sessionbeans.TimonLogic;

/**
 *
 * @author Alfonso Tames
 */
@Named(value = "um")
@SessionScoped
public class UserManager implements Serializable {
    
    @Inject
    private Registro registro;
    @Inject
    private TimonLogic tl;
    private Miembro user;
    private String email;
    private String passwd;
    private String origen;

    @Resource
    private javax.transaction.UserTransaction utx;
    
    public UserManager() {
        user = null;      
    }
    
    public boolean isValid() {
        //System.out.println("isValid...");
        boolean v = false;
        if (user != null) {
            v = true;
            //System.out.println("Es "+usuario.getNombre());
        }
        //System.out.println(v);
        return v;
    }

    public String logout() {
        
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/index.xhtml?faces-redirect=true";
    }

    public String login() {
        System.out.println("Login con: "+email+" y "+passwd+" desde "+origen);
        String r = "";
        
        Miembro u = null;
        try {
        u= tl.login(email, passwd);
        registro.setMiembro(u);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        if (u != null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Bienvenido " + u.getNombre(), null));

            if (origen != null) {

                r = origen + "?faces-redirect=true";
            } else {
                r = null;
            }
            this.user = u;

        }
        if (u == null) {
            r = null;
            System.out.println("No hay acceso para ti");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Usuario inválido...", null));
        }
        this.user = u;
        
        //System.out.println("Resultado del login: " + r);

        return r;
    }
    public String loginsinredirect() {
        origen = null;
        return login();
    }
    /**
     * @return the user
     */
    public Miembro getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(Miembro user) {
        this.user = user;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the passw
     */
    public String getPasswd() {
        return passwd;
    }

    /**
     * @param passwd the passw to set
     */
    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    /**
     * @return the origen
     */
    public String getOrigen() {
        return origen;
    }

    /**
     * @param origen the origen to set
     */
    public void setOrigen(String origen) {
        this.origen = origen;
    }

    
}
