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
package timon.entities.registro;

import java.io.Serializable;
import javax.persistence.*;

/**
 *
 * @author alfonso
 */
@Entity
@Table(uniqueConstraints={@UniqueConstraint(columnNames = {"ESTADO,CLAVE"})})
public class Seccion implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long clave;
    private String municipio;
    private String colonia;
    private Estado estado;
    private String distritoLocal;
    private String distritoFederal;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {

        if (!(object instanceof Seccion)) {
            return false;
        }
        Seccion other = (Seccion) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entidades.Seccion[ id=" + id + " ]";
    }

    /**
     * @return the clave
     */
    public Long getClave() {
        return clave;
    }

    /**
     * @param clave the clave to set
     */
    public void setClave(Long clave) {
        this.clave = clave;
    }

    /**
     * @return the municipio
     */
    public String getMunicipio() {
        return municipio;
    }

    /**
     * @param municipio the municipio to set
     */
    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    /**
     * @return the colonia
     */
    public String getColonia() {
        return colonia;
    }

    /**
     * @param colonia the colonia to set
     */
    public void setColonia(String colonia) {
        this.colonia = colonia;
    }

    /**
     * @return the estado
     */
    public Estado getEstado() {
        return estado;
    }

    /**
     * @param estado the estado to set
     */
    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    /**
     * @return the distritoLocal
     */
    public String getDistritoLocal() {
        return distritoLocal;
    }

    /**
     * @param distritoLocal the distritoLocal to set
     */
    public void setDistritoLocal(String distritoLocal) {
        this.distritoLocal = distritoLocal;
    }

    /**
     * @return the distritoFederal
     */
    public String getDistritoFederal() {
        return distritoFederal;
    }

    /**
     * @param distritoFederal the distritoFederal to set
     */
    public void setDistritoFederal(String distritoFederal) {
        this.distritoFederal = distritoFederal;
    }
    
}
