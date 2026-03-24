package teams.student.Goonies.units;

import components.upgrade.Plating;
import components.upgrade.Shield;
import components.weapon.Weapon;
import components.weapon.utility.ElectromagneticPulse;
import components.weapon.utility.Pullbeam;
import objects.entity.unit.Frame;
import objects.entity.unit.Model;
import objects.entity.unit.Style;
import objects.entity.unit.Unit;
import teams.student.Goonies.GooniesUnit;

public class Breaker extends GooniesUnit
{
    public void design()
    {


        setFrame(Frame.MEDIUM);
        setModel(Model.CRUISER);
        setStyle(Style.ORB);
        add(ElectromagneticPulse.class);
        add(Shield.class);
        add(Plating.class);

    }

    public void action()
    {
        Weapon w = getWeaponOne();
        Unit enemy = getPulseTarget();

        if (getPercentEffectiveHealth() < 0.32f && getNearbyEnemyCount(1000) > getNearbyAllyCount(1150))
        {
            beginRetreat(75);
        }

        if (inRetreat())
        {
            moveTo(getHomeBase());
            return;
        }

        if (enemy == null)
        {
            guardMid();
            return;
        }

        if (w == null)
        {
            guardMid();
            return;
        }

        if (w.onCooldown())
        {
            if (getDistance(enemy) < 800 && getNearbyEnemyCount(900) > getNearbyAllyCount(1000))
            {
                moveTo(getHomeBase());
            }
            else
            {
                moveTo(enemy);
            }

            return;
        }

        if (getDistance(enemy) > w.getRadius() * 0.82f)
        {
            moveTo(enemy);
        }
        else
        {
            circleAround(enemy);

            if (getNearbyEnemyCount((float)(w.getRadius() * 1.05f)) >= 1)
            {
                w.use();
            }
        }
    }

    private Unit getPulseTarget()
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

            if (dist > 2800)
            {
                continue;
            }

            double score = 0;
            score -= dist * 0.15;


            if (enemy.hasWeapon(ElectromagneticPulse.class))
            {
                score += 1600;
            }
            if (enemy.hasWeapon(Pullbeam.class))
            {
                score += 1500;
            }
            if (enemy.hasShield())
            {
                score += 900;
            }
            if (isHealer(enemy))
            {
                score += 850;
            }
            if (isMissileBoat(enemy))
            {
                score += 800;
            }
            if (isUtility(enemy))
            {
                score += 700;
            }
            if (enemy.getFrame() == Frame.HEAVY)
            {
                score += 200;
            }
            if (enemy.getFrame() == Frame.ASSAULT)
            {
                score += 140;
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