package teams.student.Goonies.units;

import components.upgrade.Shield;
import components.weapon.Weapon;
import components.weapon.kinetic.Autocannon;
import objects.entity.unit.Frame;
import objects.entity.unit.Model;
import objects.entity.unit.Style;
import objects.entity.unit.Unit;
import teams.student.Goonies.GooniesUnit;

public class Skirmisher extends GooniesUnit
{
    public void design()
    {
        setFrame(Frame.LIGHT);
        setModel(Model.STRIKER);
        setStyle(Style.ARROW);
        add(Autocannon.class);
        add(Shield.class);
    }

    public void action()
    {
        Weapon w = getWeaponOne();
        Unit enemy = getRaidTarget();

        if (hasShield() && getCurShield() == 0)
        {
            beginRetreat(90);
        }

        if (inRetreat())
        {
            moveTo(getHomeBase());
            attack(w);
            return;
        }

        if (enemy != null)
        {
            double dist = getDistance(enemy);

            if (shouldCharge(enemy))
            {
                if (dist > w.getMaxRange() * 0.4f)
                {
                    moveTo(enemy);
                }
                else
                {
                    circleAround(enemy);
                }
            }
            else if (dist > w.getMaxRange() * 0.95f)
            {
                moveTo(enemy);
            }
            else if (dist < w.getMaxRange() * 0.45f)
            {
                backAwayFrom(enemy);
            }
            else
            {
                circleAround(enemy);
            }

            if (dist <= w.getMaxRange() * 0.95f)
            {
                w.use(enemy);
            }
        }
        else
        {
            guardMid();
        }
    }

    private Unit getRaidTarget()
    {
        Unit best = null;
        double bestScore = -999999;

        for (Unit enemy : getPlayer().getUnits(getOpponent(), Unit.class))
        {
            if (enemy == null || !enemy.isAlive())
            {
                continue;
            }

            double dist = getDistance(enemy);

            if (dist > getMaxRange() * 4f + 300)
            {
                continue;
            }

            double score = 0;
            score -= dist * 0.15;
            score += (enemy.getMaxEffectiveHealth() - enemy.getCurEffectiveHealth()) * 0.5;

            if (isWorker(enemy))
            {
                score += 1000;
            }
            if (isMissileBoat(enemy))
            {
                score += 550;
            }
            if (isUtility(enemy))
            {
                score += 325;
            }
            if (enemy.getFrame() == Frame.LIGHT)
            {
                score += 250;
            }

            if (score > bestScore)
            {
                bestScore = score;
                best = enemy;
            }
        }

        return best;
    }
}