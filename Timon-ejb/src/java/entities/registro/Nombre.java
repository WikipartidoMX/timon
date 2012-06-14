/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package entities.registro;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

/**
 *
 * @author alfonso
 */
@Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"NOMBRE"})})
public class Nombre implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String promotor;
    private String ip;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date fecha;

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
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Nombre)) {
            return false;
        }
        Nombre other = (Nombre) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entidades.Nombre[ id=" + id + " ]";
    }

    /**
     * @return the nombre
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * @param nombre the nombre to set
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * @return the ip
     */
    public String getIp() {
        return ip;
    }

    /**
     * @param ip the ip to set
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * @return the fecha
     */
    public Date getFecha() {
        return fecha;
    }

    /**
     * @param fecha the fecha to set
     */
    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    /**
     * @return the promotor
     */
    public String getPromotor() {
        return promotor;
    }

    /**
     * @param promotor the promotor to set
     */
    public void setPromotor(String promotor) {
        this.promotor = promotor;
    }
    
}
