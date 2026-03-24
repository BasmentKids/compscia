package teams.student.Goonies.units;

import components.weapon.Weapon;
import components.weapon.economy.Drillbeam;
import objects.entity.node.Node;
import objects.entity.unit.Frame;
import objects.entity.unit.Model;
import objects.entity.unit.Style;
import objects.entity.unit.Unit;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import teams.student.Goonies.GooniesUnit;

public class Miner extends GooniesUnit
{
	private Node targetNode;

	public void design()
	{
		setFrame(Frame.LIGHT);
		setModel(Model.DESTROYER);
		setStyle(Style.BOXY);
		add(Drillbeam.class);
	}

	public void action()
	{
		if (shouldRunHome())
		{
			moveTo(getHomeBase());
			return;
		}

		targetNode = getNearestNode();
		harvest(targetNode, getWeaponOne());
	}

	@Override
	public void draw(Graphics g)
	{
		super.draw(g);
		if (targetNode != null)
		{
			g.setColor(Color.white);
			g.setLineWidth(1);
			g.drawLine(getCenterX(), getCenterY(), targetNode.getCenterX(), targetNode.getCenterY());
		}
	}

	public void harvest(Node n, Weapon w)
	{
		if (n == null || w == null)
		{
			return;
		}

		if (getDistance(n) > w.getMaxRange() * .5f)
		{
			moveTo(n);
		}
		else if (getDistance(n) < w.getMinRange() * 1.5f)
		{
			turnTo(n);
			turnAround();
			move();
		}

		w.use(n);
	}

	private boolean shouldRunHome()
	{
		Unit enemy = getNearestEnemy();

		if (enemy == null)
		{
			return false;
		}

		if (getDistance(enemy) < 700)
		{
			return true;
		}

		return getDistance(enemy) < 1100 && getNearbyEnemyCount(1100) > getNearbyAllyCount(1200);
	}
}