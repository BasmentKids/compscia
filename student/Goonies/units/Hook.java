package teams.student.Goonies.units;

import components.upgrade.Plating;
import components.upgrade.Shield;
import components.weapon.Weapon;
import components.weapon.utility.Pullbeam;
import objects.entity.unit.Frame;
import objects.entity.unit.Model;
import objects.entity.unit.Style;
import objects.entity.unit.Unit;
import teams.student.Goonies.GooniesUnit;

public class Hook extends GooniesUnit
{
    public void design()
    {
        setFrame(Frame.MEDIUM);
        setModel(Model.CRUISER);
        setStyle(Style.DAGGER);
        add(Pullbeam.class);
        add(Shield.class);
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
        Unit enemy = getHookTarget((float)(w.getMaxRange() * 2.4f));

        if (getPercentEffectiveHealth() < 0.30f && getNearbyEnemyCount(1100) > getNearbyAllyCount(1200))
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

        double dist = getDistance(enemy);

        if (dist > w.getMaxRange() * 0.86f)
        {
            moveTo(enemy);
        }
        else
        {
            circleAround(enemy);
            w.use(enemy);

            if (shouldCharge(enemy) || enemy.hasShield() || enemy.getFrame() == Frame.ASSAULT)
            {
                moveTo(enemy);
            }
            else if (dist < w.getMaxRange() * 0.40f)
            {
                backAwayFrom(enemy);
            }
        }
    }

    private Unit getHookTarget(float radius)
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

            if (dist > radius)
            {
                continue;
            }

            double score = 0;
            score -= dist * 0.10;
            score += (enemy.getMaxEffectiveHealth() - enemy.getCurEffectiveHealth()) * 0.55;

            if (isHealer(enemy))
            {
                score += 1100;
            }
            if (isMissileBoat(enemy))
            {
                score += 950;
            }
            if (isUtility(enemy))
            {
                score += 875;
            }
            if (enemy.hasShield())
            {
                score += 325;
            }
            if (enemy.getFrame() == Frame.ASSAULT)
            {
                score += 250;
            }
            if (isWorker(enemy))
            {
                score += 350;
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