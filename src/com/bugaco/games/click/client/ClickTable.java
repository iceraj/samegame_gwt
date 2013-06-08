package com.bugaco.games.click.client;

import com.bugaco.games.click.client.rsrc.CircleImages;
import com.bugaco.games.click.client.rsrc.CommonImages;
import com.bugaco.games.click.client.rsrc.SquareImages;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class ClickTable extends Composite
{
	private final CommonImages sif;
	private static final int INVALID = -1;
	private static final int EMPTY = -2;
	private static final int OUTOFBOUNDS = -3;
	private final int cols = 10;
	private final int rows = 16;
	private final int colors = 5;
	private final ImageResource[] images;
	int[][] data;
	Grid grid;
	private Label score;
	private boolean cyclick;

	public ClickTable(Label score, boolean cyclick)
	{
		GWT.log("start ClickTable");
		if (cyclick)
		{
			sif = (CommonImages) GWT.create(CircleImages.class);
		}
		else
		{
			sif = (CommonImages) GWT.create(SquareImages.class);
		}
		images = new ImageResource[] { sif.blue(), sif.red(), sif.green(), sif.yellow(), sif.black() };
		this.score = score;
		this.cyclick = cyclick;
		grid = new Grid(rows, cols);
		grid.setCellPadding(0);
		grid.setCellSpacing(0);
		grid.setStyleName("gameTable");
		data = new int[rows][cols];
		restart();
		initWidget(grid);
		GWT.log("end ClickTable");
	};

	void randomize()
	{
		GWT.log("randomize start");
		for (int a = 0; a < rows; a++)
		{
			for (int b = 0; b < cols; b++)
			{
				data[a][b] = (int) (Math.random() * colors);
			}
		}
		;
		GWT.log("randomize end");
	}

	Label getBlank()
	{
		Label l = new Label("");
		l.setWidth("24px");
		l.setHeight("24px");
		return l;
	}

	void updateUI()
	{
		GWT.log("updateUI start ");
		grid.clear();
		for (int a = 0; a < rows; a++)
		{
			for (int b = 0; b < cols; b++)
			{
				if (data[a][b] >= 0)
				{
					grid.setWidget(a, b, getWidget(data[a][b], a, b));
				}
				else
				{
					grid.setWidget(a, b, getBlank());
				}
			}
		}

		GWT.log("updateUI end ");
	}

	private void handle(int a, int b)
	{
		GWT.log("handle " + a + " " + b);
		if (data[a][b] != INVALID && data[a][b] != EMPTY && inBlock(a, b, data[a][b]))
		{
			GWT.log("handle valid " + a + " " + b);
			clear(a, b, data[a][b]);
			fall();
			if (cyclick)
			{
				while (true)
				{
					boolean tumbled = tumble();
					if (tumbled)
					{
						fall();
					}
					else
					{
						break;
					}
				}
			}
			consolidate();
			score();
			updateUI();
		}
		GWT.log("handle end");
	}

	private boolean tumble()
	{
		boolean tumbled = false;
		OUT: for (int b = 0; b < cols; b++)
		{
			for (int a = 0; a < rows; a++)
			{
				if (get(a, b) != EMPTY)
				{
					if (get(a + 1, b - 1) == EMPTY || get(a + 1, b + 1) == EMPTY)
					{
						if (get(a + 1, b - 1) == EMPTY && get(a + 1, b + 1) == EMPTY)
						{
							if (Math.random() < .5f)
							{
								data[a + 1][b - 1] = data[a][b];
								data[a][b] = EMPTY;
								drop(a + 1, b - 1);
								tumbled = true;
								break OUT;
							}
							else
							{
								data[a + 1][b + 1] = data[a][b];
								data[a][b] = EMPTY;
								drop(a + 1, b + 1);
								tumbled = true;
								break OUT;
							}
						}
						else
						{
							if (get(a + 1, b - 1) == EMPTY)
							{
								data[a + 1][b - 1] = data[a][b];
								data[a][b] = EMPTY;
								drop(a + 1, b - 1);
								tumbled = true;
								break OUT;
							}
							else
							{
								data[a + 1][b + 1] = data[a][b];
								data[a][b] = EMPTY;
								drop(a + 1, b + 1);
								tumbled = true;
								break OUT;
							}
						}
					}
					else
					{
						break;
					}
				}

			}
		}
		return tumbled;
	}

	void drop(int a, int b)
	{
		if (get(a + 1, b) == EMPTY)
		{
			int tmp = data[a + 1][b];
			data[a + 1][b] = data[a][b];
			data[a][b] = tmp;
			drop(a + 1, b);
		}
	}

	private boolean inBlock(int a, int b, int i)
	{
		return i == get(a - 1, b) || i == get(a + 1, b) || i == get(a, b - 1) || i == get(a, b + 1);
	}

	private int get(int a, int b)
	{
		if (a >= 0 && a < rows && b >= 0 && b < cols)
		{
			return data[a][b];
		}
		else
		{
			return OUTOFBOUNDS;
		}
	}

	private void consolidate()
	{
		for (int b = 0; b < cols; b++)
		{
			boolean isEmpty = true;
			for (int a = 0; a < rows; a++)
			{
				if (data[a][b] != EMPTY)
				{
					isEmpty = false;
					break;
				}
			}
			if (isEmpty)
			{
				for (int c = b; c < cols - 1; c++)
				{
					for (int a = 0; a < rows; a++)
					{
						data[a][c] = data[a][c + 1];
					}
				}
				for (int a = 0; a < rows; a++)
				{
					data[a][cols - 1] = EMPTY;
				}
			}
		}
	}

	private void score()
	{
		GWT.log("score start");
		int score = 0;
		for (int a = 0; a < rows; a++)
		{
			for (int b = 0; b < cols; b++)
			{
				if (data[a][b] >= 0)
				{
					score++;
				}
			}
		}
		this.score.setText(String.valueOf(score));
		GWT.log("score is " + score);
	}

	private void fall()
	{
		GWT.log("fall start");
		for (int b = 0; b < cols; b++)
		{
			for (int a = rows - 1; a > 0; a--)
			{
				if (data[a][b] == INVALID)
				{
					for (int aa = a; aa > 0; aa--)
					{
						data[aa][b] = data[aa - 1][b];
					}
					data[0][b] = EMPTY;
					a++;
				}
			}
		}
		GWT.log("fall end");
	}

	private void clear(int a, int b, int c)
	{
		if (a < rows && b < cols && a >= 0 && b >= 0)
		{
			if (data[a][b] == c)
			{
				data[a][b] = INVALID;
				GWT.log("Clear " + a + " " + b);
				clear(a, b - 1, c);
				clear(a, b + 1, c);
				clear(a - 1, b, c);
				clear(a + 1, b, c);
			}
		}
	}

	private Widget getWidget(int i, final int a, final int b)
	{
		Image img = new Image(images[i].getURL());
		img.setWidth("24px");
		img.setHeight("24px");
		Anchor l = new Anchor("" + i);
		img.addClickHandler(new ClickHandler()
		{

			@Override
			public void onClick(ClickEvent event)
			{
				handle(a, b);
			}
		});
		return img;
	}

	public void restart()
	{
		randomize();
		updateUI();
		score();
	}
}
