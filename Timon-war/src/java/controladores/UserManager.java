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

import entities.Miembro;
import java.io.Serializable;
import javax.annotation.Resource;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Alfonso Tames
 */
@Named(value = "um")
@SessionScoped
public class UserManager implements Serializable {
    
    @Inject
    private Registro registro;
    
    private Miembro user;
    private String email;
    private String passw;
    private String origen;
    private String estiforma;
    @PersistenceContext(unitName = "Timon-warPU")
    private EntityManager em;
    @Resource
    private javax.transaction.UserTransaction utx;
    
    public UserManager() {
        user = null;
        estiforma = "none";        
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
        return "index.xhtml?faces-redirect=true";
    }

    public String login() {
        System.out.println("Login con: "+email+" y "+passw);
        String r = "";
        
        Miembro u = null;
        try {
        u= (Miembro)em.createQuery("select m from Miembro m "
                + "where m.email=:email and m.password=:passw").setParameter("email", email)
                .setParameter("passw", passw).getSingleResult();
        registro.setMiembro(u);
        } catch (Exception e) {
            
        }
        if (u != null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Bienvenido " + u.getNombre(), null));

            if (origen != null) {

                r = origen + "?faces-redirect=true";
            } else {
                r = null;
                setEstiforma("block");
            }
            this.user = u;
            setEstiforma("none");
        }
        if (u == null) {
            r = null;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Usuario inválido...", null));
            if (origen == null) {
                setEstiforma("block");
            }
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
    public String getPassw() {
        return passw;
    }

    /**
     * @param passw the passw to set
     */
    public void setPassw(String passw) {
        this.passw = passw;
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

    /**
     * @return the estiforma
     */
    public String getEstiforma() {
        return estiforma;
    }

    /**
     * @param estiforma the estiforma to set
     */
    public void setEstiforma(String estiforma) {
        this.estiforma = estiforma;
    }

    
}
