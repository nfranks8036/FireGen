package net.noahf.firegen.backend.access;

import net.noahf.firegen.backend.Main;
import net.noahf.firegen.backend.database.structure.Unit;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UnitManagerService {

    public List<Unit> getAllUnits() {

        return Main.db.datastore().find(Unit.class).stream().toList();
    }

}