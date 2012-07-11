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



import entities.votacionydebate.Opcion;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.naming.Context;
import javax.naming.InitialContext;
import sessionbeans.VotoYDebateLogic;

/**
 *
 * @author Alfonso Tamés
 */
@FacesConverter(forClass=Opcion.class,value="opcion")
public class OpcionConverter implements Converter {
    
    // Por alguna razon no esta inyectando el session bean!!!    
    //@Inject
    //VotoYDebateLogic vl;    

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value.trim().equals("")) {
            return null;
        } else {
            try {
                long id = Long.parseLong(value);
                // A la antigua :(
                Context ctx = new InitialContext();
                VotoYDebateLogic vl = (VotoYDebateLogic) ctx.lookup("java:global/Timon/Timon-ejb/VotoYDebateLogic");
                Opcion o = vl.getOpcion(id);
                return o;
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
            return String.valueOf(((Opcion)value).getId());
        }
    }
    
}
