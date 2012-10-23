package me.azazad.turrets.persistence;

import java.io.IOException;
import java.util.Collection;
import me.azazad.turrets.Turret;

public interface TurretDatabase{
    public Collection<Turret> loadTurrets() throws IOException;
    
    public void saveTurrets(Collection<Turret> turrets) throws IOException;
    
    public void saveTurretsForReload(Collection<Turret> turrets) throws IOException;

	public void reloadTurrets() throws IOException;
}