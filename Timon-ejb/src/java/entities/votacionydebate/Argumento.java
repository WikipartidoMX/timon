/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package entities.votacionydebate;

import entities.registro.Miembro;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

/**
 *
 * @author alfonso
 */
@Entity
public class Argumento implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    private Votacion votacion;
    
    @ManyToOne
    private Miembro miembro;
    
    @ManyToOne
    private Opcion opcion;
    
    private String discurso;
    private long rating;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date fecha;
    
    @ManyToOne
    private Argumento parent;
    
    
    

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
        if (!(object instanceof Argumento)) {
            return false;
        }
        Argumento other = (Argumento) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.votacionydebate.Argumento[ id=" + id + " ]";
    }

    /**
     * @return the miembro
     */
    public Miembro getMiembro() {
        return miembro;
    }

    /**
     * @param miembro the miembro to set
     */
    public void setMiembro(Miembro miembro) {
        this.miembro = miembro;
    }

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
     * @return the discurso
     */
    public String getDiscurso() {
        return discurso;
    }

    /**
     * @param discurso the discurso to set
     */
    public void setDiscurso(String discurso) {
        this.discurso = discurso;
    }

    /**
     * @return the rating
     */
    public long getRating() {
        return rating;
    }

    /**
     * @param rating the rating to set
     */
    public void setRating(long rating) {
        this.rating = rating;
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
     * @return the parent
     */
    public Argumento getParent() {
        return parent;
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(Argumento parent) {
        this.parent = parent;
    }

    /**
     * @return the votacion
     */
    public Votacion getVotacion() {
        return votacion;
    }

    /**
     * @param votacion the votacion to set
     */
    public void setVotacion(Votacion votacion) {
        this.votacion = votacion;
    }
    
}
