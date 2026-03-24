package teams.student.Goonies.units;

import components.upgrade.Munitions;
import components.upgrade.Plating;
import components.weapon.Weapon;
import components.weapon.energy.HeavyLaser;
import objects.entity.unit.Frame;
import objects.entity.unit.Model;
import objects.entity.unit.Style;
import objects.entity.unit.Unit;
import teams.student.Goonies.GooniesUnit;

public class Sniper extends GooniesUnit
{
    public void design()
    {
        setFrame(Frame.HEAVY);
        setModel(Model.ARTILLERY);
        setStyle(Style.CANNON);

        add(HeavyLaser.class);
        add(Munitions.class);
        add(Plating.class);
    }

    public void action()
    {
        Weapon w = getWeaponOne();
        if (w == null)
        {
            guardMid();
            return;
        }
        Unit enemy = getBestEnemyInRadius((float)(w.getMaxRange() * 1.8f));

        if (shouldRetreat())
        {
            beginRetreat(90);
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
        double minRange = w.getMinRange();
        double maxRange = w.getMaxRange();

        if (shouldCharge(enemy))
        {
            if (dist > Math.max(minRange * 1.15f, maxRange * 0.55f))
            {
                moveTo(enemy);
            }
            else
            {
                circleAround(enemy);
            }
        }
        else if (dist > maxRange * 0.92f)
        {
            moveTo(enemy);
        }
        else if (dist < Math.max(minRange * 1.7f, maxRange * 0.6f))
        {
            backAwayFrom(enemy);
        }
        else
        {
            circleAround(enemy);
        }

        if (dist <= maxRange * 0.95f && dist >= minRange * 1.1f)
        {
            w.use(enemy);
        }
    }
}