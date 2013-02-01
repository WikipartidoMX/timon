/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timon.entities.asambleas;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import timon.entities.registro.Estado;
import timon.entities.registro.Miembro;

/**
 *
 * @author alfonso
 */
@Entity
public class Asamblea implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @OneToOne
    private Estado estado;
    private String lugar;
    private String direccion;
    private double latitud;
    private double longitud;
    @OneToMany
    private List<Miembro> confirmados;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date fecha;
    private int quorum;
    @OneToMany
    private List<Miembro> comiteOrganizador;
    
    
    

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
        if (!(object instanceof Asamblea)) {
            return false;
        }
        Asamblea other = (Asamblea) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "timon.entities.asambleas.Asamblea[ id=" + id + " ]";
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
     * @return the lugar
     */
    public String getLugar() {
        return lugar;
    }

    /**
     * @param lugar the lugar to set
     */
    public void setLugar(String lugar) {
        this.lugar = lugar;
    }

    /**
     * @return the direccion
     */
    public String getDireccion() {
        return direccion;
    }

    /**
     * @param direccion the direccion to set
     */
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    /**
     * @return the latitud
     */
    public double getLatitud() {
        return latitud;
    }

    /**
     * @param latitud the latitud to set
     */
    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    /**
     * @return the longitud
     */
    public double getLongitud() {
        return longitud;
    }

    /**
     * @param longitud the longitud to set
     */
    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    /**
     * @return the confirmados
     */
    public List<Miembro> getConfirmados() {
        return confirmados;
    }

    /**
     * @param confirmados the confirmados to set
     */
    public void setConfirmados(List<Miembro> confirmados) {
        this.confirmados = confirmados;
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
     * @return the quorum
     */
    public int getQuorum() {
        return quorum;
    }

    /**
     * @param quorum the quorum to set
     */
    public void setQuorum(int quorum) {
        this.quorum = quorum;
    }

    /**
     * @return the comiteOrganizador
     */
    public List<Miembro> getComiteOrganizador() {
        return comiteOrganizador;
    }

    /**
     * @param comiteOrganizador the comiteOrganizador to set
     */
    public void setComiteOrganizador(List<Miembro> comiteOrganizador) {
        this.comiteOrganizador = comiteOrganizador;
    }
    
}
