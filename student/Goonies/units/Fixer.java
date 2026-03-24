package teams.student.Goonies.units;

import components.upgrade.Plating;
import components.weapon.Weapon;
import components.weapon.economy.Collector;
import components.weapon.economy.Drillbeam;
import components.weapon.utility.HeavyRepairBeam;
import objects.entity.unit.Frame;
import objects.entity.unit.Model;
import objects.entity.unit.Style;
import objects.entity.unit.Unit;
import teams.student.Goonies.GooniesUnit;

public class Fixer extends GooniesUnit
{
    public void design()
    {
        setFrame(Frame.HEAVY);
        setModel(Model.BASTION);
        setStyle(Style.SHARK);
        add(HeavyRepairBeam.class);
        add(Plating.class);
        add(Plating.class);
    }

    public void action()
    {
        Unit ally = getHealTarget();
        Unit enemy = getNearestEnemy();

        if (enemy != null && getDistance(enemy) < 700 && getPercentEffectiveHealth() < 0.75f)
        {
            moveTo(getHomeBase());
            return;
        }

        if (ally != null)
        {
            Weapon beam = getWeaponOne();
            if (beam != null)
            {
                if (getDistance(ally) > beam.getMaxRange() * 0.85f)
                {
                    moveTo(ally);
                }
                else
                {
                    turnTo(ally);
                }
                beam.use(ally);
            }
        }
        else
        {
            guardMid();
        }
    }

    public Unit getHealTarget()
    {
        Unit best = null;
        double bestScore = -1;

        for (Unit ally : getAllies())
        {
            if (ally == null || ally == this || !ally.isAlive())
            {
                continue;
            }

            if (ally.getPercentEffectiveHealth() >= 1.0f)
            {
                continue;
            }

            double score = (1.0 - ally.getPercentEffectiveHealth()) * 1000;

            if (!isWorkerAlly(ally))
            {
                score += 250;
            }

            if (ally instanceof Tank || ally instanceof Fighter || ally instanceof Hook)
            {
                score += 150;
            }

            score -= getDistance(ally) * 0.08;

            if (score > bestScore)
            {
                bestScore = score;
                best = ally;
            }
        }

        return best;
    }

    private boolean isWorkerAlly(Unit ally)
    {
        Weapon w = ally.getWeaponOne();
        return w instanceof Collector || w instanceof Drillbeam;
    }
}