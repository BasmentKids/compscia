package teams.student.Goonies.units;

import components.upgrade.Munitions;
import components.upgrade.Plating;
import components.weapon.Weapon;
import components.weapon.energy.HeavyLaser;
import components.weapon.energy.Laser;
import components.weapon.kinetic.Autocannon;
import components.weapon.kinetic.HeavyAutocannon;
import objects.entity.unit.Frame;
import objects.entity.unit.Model;
import objects.entity.unit.Style;
import objects.entity.unit.Unit;
import teams.student.Goonies.GooniesUnit;

public class Fighter extends GooniesUnit
{

	public void design()
	{
		setFrame(Frame.ASSAULT);
		setModel(Model.DESTROYER);
		setStyle(Style.ARROW);
		add(Autocannon.class);
		add(HeavyLaser.class);
		add(Munitions.class);
		add(Plating.class);
	}

	public void action()
	{
		Weapon gun = getWeaponOne();
		Weapon laser = getWeaponTwo();
		Unit enemy = getGroupTarget((float)(getMaxRange() * 2.6f + 650));

		if (getPercentEffectiveHealth() < 0.28f && getNearbyEnemyCount(1200) > getNearbyAllyCount(1200))
		{
			beginRetreat(75);
		}

		if (inRetreat())
		{
			moveTo(getHomeBase());
			attack(gun);
			attack(laser);
			return;
		}

		if (enemy == null)
		{
			guardMid();
			return;
		}

		double dist = getDistance(enemy);
		double maxRange = getMaxRange();
		boolean commit = shouldCharge(enemy) || enemy.hasShield() || enemy.getFrame() == Frame.ASSAULT;

		if (commit)
		{
			if (dist > maxRange * 0.62f)
			{
				moveTo(enemy);
			}
			else
			{
				circleAround(enemy);
			}
		}
		else if (dist > maxRange * 0.82f)
		{
			moveTo(enemy);
		}
		else if (enemy.getMaxRange() + 150 < maxRange && dist < maxRange * 0.45f)
		{
			backAwayFrom(enemy);
		}
		else
		{
			circleAround(enemy);
		}

		if (gun != null && dist <= gun.getMaxRange() * 0.96f)
		{
			gun.use(enemy);
		}

		if (laser != null && dist <= laser.getMaxRange() * 0.96f)
		{
			laser.use(enemy);
		}
	}
}