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
package converters;

import controladores.UserManager;
import entities.registro.Miembro;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import sessionbeans.TimonLogic;

/**
 *
 * @author alfonso
 */
@FacesConverter(forClass=Miembro.class,value="miembro")
public class MiembroConverter implements Converter {
    
    // Por alguna razon no esta inyectando el session bean!!!
    //@Inject
    //TimonLogic tl;
    

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value.trim().equals("")) {
            return null;
        } else {
            try {
                long id = Long.parseLong(value);
                // A la antigua :(
                Context ctx = new InitialContext();
                TimonLogic tl = (TimonLogic) ctx.lookup("java:global/Timon/Timon-ejb/TimonLogic");
                Miembro m = tl.getMiembro(id);
                return m;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return null;
            }
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value == null) {
            return "";
        } else {
            return String.valueOf(((Miembro)value).getId());
        }
    }


    
}
