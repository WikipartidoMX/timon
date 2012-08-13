/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timon.controladores.votacion;

import timon.entities.votacionydebate.ImagenOpcion;
import timon.entities.votacionydebate.Opcion;

/**
 *
 * @author alfonso
 */
public class OpcionMasImagen {

    private Opcion opcion;
    private ImagenOpcion imagen;

    /**
     * @return the opcion
     */
    public Opcion getOpcion() {
        return opcion;
    }

    /**
     * @param opcion the opcion to set
     */
    public void setOpcion(Opcion opcion) {
        this.opcion = opcion;
    }

    /**
     * @return the imagen
     */
    public ImagenOpcion getImagen() {
        return imagen;
    }

    /**
     * @param imagen the imagen to set
     */
    public void setImagen(ImagenOpcion imagen) {
        this.imagen = imagen;
    }
}
