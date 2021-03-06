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
package timon.converters;

import java.util.logging.Level;
import java.util.logging.Logger;
import timon.entities.registro.Miembro;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.naming.Context;
import javax.naming.InitialContext;
import timon.sessionbeans.TimonLogic;

/**
 *
 * @author Alfonso Tames
 */
@FacesConverter(forClass=Miembro.class,value="miembro")
public class MiembroConverter implements Converter {
    private static final Logger mrlog = Logger.getLogger(MiembroConverter.class.getName());
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
                mrlog.log(Level.FINE,"Buscando al miembro con id {0}",id);
                Miembro m = tl.getMiembro(id);
                return m;
            } catch (Exception e) {
                System.out.println("Hubo un error: "+e.getMessage());
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
