/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controladores;

import java.io.Serializable;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import sessionbeans.TimonLogic;

/**
 *
 * @author alfonso
 */
@Named(value = "test")
@SessionScoped
public class TestController implements Serializable {
    @EJB
    private TimonLogic tl;
    
    
    public TestController() {
        tl.echo("Alfonso");
    }
    
}
