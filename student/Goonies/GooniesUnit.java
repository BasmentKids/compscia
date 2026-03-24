package teams.student.Goonies;

import components.weapon.Weapon;
import components.weapon.economy.Collector;
import components.weapon.economy.Drillbeam;
import components.weapon.explosive.HeavyMissile;
import components.weapon.explosive.Missile;
import components.weapon.utility.ElectromagneticPulse;
import components.weapon.utility.HeavyRepairBeam;
import components.weapon.utility.Pullbeam;
import components.weapon.utility.RepairBeam;
import components.weapon.utility.SpeedBoost;
import objects.entity.unit.Frame;
import objects.entity.unit.Unit;
import objects.GameObject;
import engine.states.Game;
import org.newdawn.slick.Graphics;

import java.util.List;

public abstract class GooniesUnit extends Unit
{
	private static int orbitCounter = 0;
	private static final int MAX_GROUPS = 8;
	private static Unit[] groupTargets = new Unit[MAX_GROUPS];

	private int retreatTimer = 0;

	private final int orbitDirection = (orbitCounter++ % 2 == 0) ? 1 : -1;

	public Goonies getPlayer()
	{
		return (Goonies) super.getPlayer();
	}

	public void movement()
	{
		//only move out when part of da group yo
		if (isCombatUnit() && getGroupIndex() < 0)
		{
			guardMid();
			return;
		}

		if (inRetreat())
		{
			retreatTimer--;
			moveTo(getHomeBase());
			return;
		}

		if (shouldRetreat())
		{
			beginRetreat(90);
			moveTo(getHomeBase());
			return;
		}

		Unit enemy = getBestEnemyInRadius((float)(getMaxRange() * 3f + 700));

		if (enemy == null)
		{
			guardMid();
			return;
		}

		double dist = getDistance(enemy);
		double myRange = getMaxRange();
		double enemyRange = enemy.getMaxRange();

		if (shouldCharge(enemy))
		{
			if (dist > myRange * 0.65f)
			{
				moveTo(enemy);
			}
			else
			{
				turnTo(enemy);
			}
		}
		else if (dist > myRange * 0.85f)
		{
			moveTo(enemy);
		}
		else if (myRange > enemyRange + 125 && dist < myRange * 0.52f)
		{
			backAwayFrom(enemy);
		}
		else
		{
			circleAround(enemy);
		}
	}


	protected void circleAround(Unit enemy)
	{
		if (enemy == null || !enemy.isAlive())
		{
			return;
		}
		float angleToEnemy = getAngleToward(enemy.getCenterX(), enemy.getCenterY());
		float orbitAngle = angleToEnemy + orbitDirection * 90f;
		while (orbitAngle >= 360f) orbitAngle -= 360f;
		while (orbitAngle < 0f) orbitAngle += 360f;
		move(orbitAngle);
	}

	public void action()
	{
		attack(getWeaponOne());
		attack(getWeaponTwo());
		movement();
	}

	public void attack(Weapon w)
	{
		if (w == null)
		{
			return;
		}

		float radius = (float) (w.getMaxRange() * 0.95f);
		Unit enemy = getGroupTarget(radius);

		if (enemy != null)
		{
			w.use(enemy);
		}
	}



	protected Unit getLowestHealthEnemyInRadius(float radius)
	{
		Goonies player = getPlayer();
		if (player == null || getOpponent() == null)
		{
			return null;
		}
		List<Unit> enemies = player.getUnits(getOpponent(), Unit.class);
		Unit best = null;
		double bestHealth = Double.MAX_VALUE;
		for (Unit e : enemies)
		{
			if (e == null || !e.isAlive() || getDistance(e) > radius)
			{
				continue;
			}
			double h = e.getCurEffectiveHealth();
			if (e.getBlock() < 1.0)
			{
				h = h / (1.0 - e.getBlock());
			}
			if (h < bestHealth)
			{
				bestHealth = h;
				best = e;
			}
		}
		return best;
	}

	protected Unit getBestEnemyInRadius(float radius)
	{
		Goonies player = getPlayer();
		if (player == null || getOpponent() == null)
		{
			return null;
		}
		List<Unit> enemies = player.getUnits(getOpponent(), Unit.class);
		Unit best = null;
		double bestScore = -999999;

		for (Unit enemy : enemies)
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
			score += (radius - dist) * 0.22;
			score += (enemy.getMaxEffectiveHealth() - enemy.getCurEffectiveHealth()) * 1.15;
			score += Math.max(0, 2200 - enemy.getCurEffectiveHealth()) * 0.22;


			if (enemy.hasWeapon(Pullbeam.class))
			{
				score += 2000;
			}
			if (enemy.hasWeapon(ElectromagneticPulse.class))
			{
				score += 1800;
			}

			if (isHealer(enemy))
			{
				score += 1300;
			}
			if (isMissileBoat(enemy))
			{
				score += 900;
			}
			if (isUtility(enemy))
			{
				score += 775;
			}
			if (isWorker(enemy))
			{
				score += 750;
			}
			if (enemy.hasShield())
			{
				score += 300;
			}
			if (enemy.getMaxRange() > getMaxRange() + 150)
			{
				score += 350;
			}
			if (enemy.getFrame() == Frame.LIGHT)
			{
				score += 120;
			}
			if (enemy.getFrame() == Frame.HEAVY)
			{
				score += 70;
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


	protected Unit getGroupTarget(float radius)
	{
		int group = getGroupIndex();
		if (group < 0 || group >= MAX_GROUPS)
		{
			return getBestEnemyInRadius(radius);
		}

		Unit current = groupTargets[group];
		if (current != null && current.isAlive() && getDistance(current) <= radius * 1.5f)
		{
			return current;
		}

		Unit best = getBestEnemyInRadius(radius);
		groupTargets[group] = best;
		return best;
	}



	protected int getGroupIndex()
	{
		Goonies player = getPlayer();
		if (player == null)
		{
			return -1;
		}

		int fighters = player.countMyUnits(teams.student.Goonies.units.Fighter.class);
		int fixers = player.countMyUnits(teams.student.Goonies.units.Fixer.class);
		int hooks = player.countMyUnits(teams.student.Goonies.units.Hook.class);
		int breakers = player.countMyUnits(teams.student.Goonies.units.Breaker.class);

		int maxGroups = Math.min(
				Math.min(fighters / 4, fixers / 2),
				Math.min(hooks / 2, breakers / 2));

		if (maxGroups <= 0)
		{
			return -1;
		}

		if (this instanceof teams.student.Goonies.units.Fighter)
		{
			int index = getTypeIndex(teams.student.Goonies.units.Fighter.class);
			int group = index / 4;
			return group < maxGroups ? group : -1;
		}
		if (this instanceof teams.student.Goonies.units.Fixer)
		{
			int index = getTypeIndex(teams.student.Goonies.units.Fixer.class);
			int group = index / 2;
			return group < maxGroups ? group : -1;
		}
		if (this instanceof teams.student.Goonies.units.Hook)
		{
			int index = getTypeIndex(teams.student.Goonies.units.Hook.class);
			int group = index / 2;
			return group < maxGroups ? group : -1;
		}
		if (this instanceof teams.student.Goonies.units.Breaker)
		{
			int index = getTypeIndex(teams.student.Goonies.units.Breaker.class);
			int group = index / 2;
			return group < maxGroups ? group : -1;
		}

		return -1;
	}

	private int getTypeIndex(Class<? extends Unit> clazz)
	{
		int index = 0;
		for (Unit ally : getAllies())
		{
			if (ally == null || !ally.isAlive())
			{
				continue;
			}
			if (clazz.isInstance(ally))
			{
				if (ally == this)
				{
					return index;
				}
				index++;
			}
		}
		return index;
	}

	protected boolean isCombatUnit()
	{
		return this instanceof teams.student.Goonies.units.Fighter
				|| this instanceof teams.student.Goonies.units.Fixer
				|| this instanceof teams.student.Goonies.units.Hook
				|| this instanceof teams.student.Goonies.units.Breaker;
	}

	protected boolean shouldRetreat()
	{
		Unit enemy = getNearestEnemy();

		if (enemy == null)
		{
			return false;
		}

		int nearbyEnemies = getNearbyEnemyCount(1200);
		int nearbyAllies = getNearbyAllyCount(1200);

		if (getPercentEffectiveHealth() < 0.35f && nearbyEnemies > nearbyAllies)
		{
			return true;
		}

		if (hasShield() && getCurShield() == 0 && getPercentEffectiveHealth() < 0.6f && nearbyEnemies > nearbyAllies + 1)
		{
			return true;
		}

		return false;
	}

	protected boolean shouldCharge(Unit enemy)
	{
		return enemy != null && (isMissileBoat(enemy) || isUtility(enemy) || isHealer(enemy) || enemy.getMaxRange() > getMaxRange() + 125);
	}

	protected int getNearbyEnemyCount(float radius)
	{
		int count = 0;

		for (Unit enemy : getPlayer().getUnits(getOpponent(), Unit.class))
		{
			if (enemy != null && enemy.isAlive() && getDistance(enemy) <= radius)
			{
				count++;
			}
		}

		return count;
	}

	protected int getNearbyAllyCount(float radius)
	{
		int count = 0;

		for (Unit ally : getAllies())
		{
			if (ally != null && ally != this && ally.isAlive() && getDistance(ally) <= radius)
			{
				count++;
			}
		}

		return count;
	}

	protected boolean isWorker(Unit u)
	{
		Weapon w = u.getWeaponOne();
		return w instanceof Collector || w instanceof Drillbeam;
	}

	protected boolean isHealer(Unit u)
	{
		Weapon w1 = u.getWeaponOne();
		Weapon w2 = u.getWeaponTwo();

		return w1 instanceof RepairBeam || w1 instanceof HeavyRepairBeam || w2 instanceof RepairBeam || w2 instanceof HeavyRepairBeam;
	}

	protected boolean isMissileBoat(Unit u)
	{
		Weapon w1 = u.getWeaponOne();
		Weapon w2 = u.getWeaponTwo();

		return w1 instanceof Missile || w1 instanceof HeavyMissile || w2 instanceof Missile || w2 instanceof HeavyMissile;
	}

	protected boolean isUtility(Unit u)
	{
		Weapon w1 = u.getWeaponOne();
		Weapon w2 = u.getWeaponTwo();

		return w1 instanceof Pullbeam || w1 instanceof ElectromagneticPulse || w1 instanceof SpeedBoost
				|| w2 instanceof Pullbeam || w2 instanceof ElectromagneticPulse || w2 instanceof SpeedBoost;
	}

	protected void backAwayFrom(Unit enemy)
	{
		turnTo(enemy);
		turnAround();
		move();
	}

	protected void beginRetreat(int frames)
	{
		if (frames > retreatTimer)
		{
			retreatTimer = frames;
		}
	}

	protected boolean inRetreat()
	{
		return retreatTimer > 0;
	}

	protected void guardMid()
	{
		float baseX = getHomeBase().getX();
		float baseY = getHomeBase().getY();

		//phase 2 detection
		boolean phaseTwo = false;
		Unit enemy = getNearestEnemy();
		if (enemy != null && getDistance(enemy) < 2200)
		{
			phaseTwo = true;
		}

		float xFactor;
		float yOffset = 0;

		//how ts work:
		//phase 1:
		//   front (toward center): fighters flanking Breakers
		//   back (toward base): Hooks flanking, Fixers on offsides basically
		// phase 2:
		//   fghters press further forward
		//   hooks move up to where Breakers were
		//   breakers fall back slightly
		//   fixers stay behind to repair
		if (this instanceof teams.student.Goonies.units.Breaker)
		{
			xFactor = phaseTwo ? 0.40f : 0.35f;
			yOffset = 0;
		}
		else if (this instanceof teams.student.Goonies.units.Fighter)
		{
			xFactor = phaseTwo ? 0.30f : 0.32f;
			yOffset = (getCenterY() < baseY) ? -450f : 450f;
		}
		else if (this instanceof teams.student.Goonies.units.Hook)
		{
			xFactor = phaseTwo ? 0.35f : 0.42f;
			yOffset = (getCenterY() < baseY) ? -350f : 350f;
		}
		else if (this instanceof teams.student.Goonies.units.Fixer)
		{
			xFactor = phaseTwo ? 0.48f : 0.45f;
			yOffset = (getCenterY() < baseY) ? -500f : 500f;
		}
		else
		{
			//rally near the middle for regular workers
			xFactor = 0.40f;
			yOffset = 0;
		}

		float targetX = baseX * xFactor;
		float targetY = baseY + yOffset;
		moveTo(targetX, targetY);
	}

	public void draw(Graphics g)
	{
	}



	public void smartMoveTo(float x, float y)
	{
		moveTo(x, y);
	}

	public void smartMoveTo(GameObject o)
	{
		if (o == null)
		{
			return;
		}
		moveTo(o.getCenterX(), o.getCenterY());
	}

	public void circleAroundTarget(float targetX, float targetY, float radius)
	{
		float px = getCenterX();
		float py = getCenterY();
		float vx = targetX - px;
		float vy = targetY - py;
		float distanceToTarget = (float) Math.sqrt(vx * vx + vy * vy);

		if (distanceToTarget > radius)
		{
			moveTo(targetX, targetY);
		}
		else
		{
			turnTo(targetX, targetY);
			turn(90);
			move();
		}
	}

	public void avoidOutOfBounds()
	{
		int adjustment = 200;
		int edgeBuffer = 200;

		if (getY() < Game.getMapTopEdge() + edgeBuffer)
		{
			moveTo(getX(), Game.getMapTopEdge() + adjustment);
		}
		else if (getY() > Game.getMapBottomEdge() - edgeBuffer)
		{
			moveTo(getX(), Game.getMapBottomEdge() - adjustment);
		}
		else if (getX() < Game.getMapLeftEdge() + edgeBuffer)
		{
			moveTo(Game.getMapLeftEdge() + adjustment, getY());
		}
		else if (getX() > Game.getMapRightEdge() - edgeBuffer)
		{
			moveTo(Game.getMapRightEdge() - adjustment, getY());
		}
	}

	public float timeBeforeThreat(Unit enemyFighter, float safetyMultiplier)
	{
		if (enemyFighter == null)
		{
			return Float.POSITIVE_INFINITY;
		}

		float dx = enemyFighter.getCenterX() - getCenterX();
		float dy = enemyFighter.getCenterY() - getCenterY();
		float distance = (float) Math.sqrt(dx * dx + dy * dy);

		float minSafeDistance = enemyFighter.getMaxRange() * safetyMultiplier;
		if (distance <= minSafeDistance)
		{
			return 0;
		}

		float relativeSpeed = enemyFighter.getMaxSpeed();
		if (relativeSpeed <= 0)
		{
			return Float.POSITIVE_INFINITY;
		}

		return (distance - minSafeDistance) / relativeSpeed;
	}

	public float timeToMove(float desiredAngle)
	{
		float curAngle = getRotation();
		float diff = desiredAngle - curAngle;

		while (diff > 180f) diff -= 360f;
		while (diff < -180f) diff += 360f;
		diff = Math.abs(diff);

		return diff / 90f;
	}
}