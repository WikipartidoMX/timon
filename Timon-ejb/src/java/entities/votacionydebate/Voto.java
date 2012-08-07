/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package entities.votacionydebate;

import entities.registro.Miembro;
import java.io.Serializable;
import javax.persistence.*;

/**
 *
 * @author Alfonso Tames
 */
@Entity
public class Voto implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Miembro miembro;
    @ManyToOne
    private Opcion opcion;
    private long rank;
    @ManyToOne
    private Miembro delegado;
    @ManyToOne
    private Votacion votacion;
    

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
        if (!(object instanceof Voto)) {
            return false;
        }
        Voto other = (Voto) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.votacionydebate.Voto[ id=" + id + " ]";
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
     * @return the rank
     */
    public long getRank() {
        return rank;
    }

    /**
     * @param rank the rank to set
     */
    public void setRank(long rank) {
        this.rank = rank;
    }

    /**
     * @return the delegado
     */
    public Miembro getDelegado() {
        return delegado;
    }

    /**
     * @param delegado the delegado to set
     */
    public void setDelegado(Miembro delegado) {
        this.delegado = delegado;
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
