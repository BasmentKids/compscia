package teams.student.Goonies.units;

import components.upgrade.HeavyPlating;
import components.upgrade.Plating;
import components.weapon.Weapon;
import components.weapon.kinetic.HeavyAutocannon;
import objects.entity.unit.Frame;
import objects.entity.unit.Model;
import objects.entity.unit.Style;
import objects.entity.unit.Unit;
import teams.student.Goonies.GooniesUnit;

public class Tank extends GooniesUnit
{
    public void design()
    {
        setFrame(Frame.HEAVY);
        setModel(Model.CRUISER); //destroyer
        setStyle(Style.CRESCENT);
        add(HeavyAutocannon.class);
        add(Plating.class);
        add(HeavyPlating.class);
    }

    public void action()
    {
        Weapon w = getWeaponOne();
        if (w == null)
        {
            guardMid();
            return;
        }
        Unit enemy = getBestEnemyInRadius((float)(w.getMaxRange() * 3.1f + 950));

        if (getPercentEffectiveHealth() < 0.24f && getNearbyEnemyCount(1200) > getNearbyAllyCount(1200))
        {
            beginRetreat(75);
        }

        if (inRetreat())
        {
            moveTo(getHomeBase());
            attack(w);
            return;
        }

        if (enemy == null)
        {
            guardMid();
            return;
        }

        double dist = getDistance(enemy);
        boolean commit = shouldCharge(enemy) || enemy.hasShield() || enemy.getFrame() == Frame.ASSAULT || isHealer(enemy);

        if (commit)
        {
            if (dist > w.getMaxRange() * 0.58f)
            {
                moveTo(enemy);
            }
            else
            {
                circleAround(enemy);
            }
        }
        else if (dist > w.getMaxRange() * 0.78f)
        {
            moveTo(enemy);
        }
        else
        {
            circleAround(enemy);
        }

        if (dist <= w.getMaxRange() * 0.97f)
        {
            w.use(enemy);
        }
    }
}