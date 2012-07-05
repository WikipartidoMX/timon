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

import entities.registro.Miembro;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import sessionbeans.TimonLogic;

/**
 *
 * @author alfonso
 */
@FacesConverter("miembro")
public class MiembroConverter implements Converter {
    
    @Inject
    TimonLogic tl;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value.trim().equals("")) {
            return null;
        } else {
            try {
                System.out.println("Conviertiendo: "+value);
                long id = Long.parseLong(value);
                System.out.println("buscando "+id);
                Miembro m = tl.getMiembro(id);
                System.out.println("regreso a "+m.getNombre());
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
