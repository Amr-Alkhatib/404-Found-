package de.tum.cit.fop.maze.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.*;

/**
 * GameMap class for handling the maze world.
 * It uses parsed map data to set up walls, traps, exits, and other game elements.
 */
public class GameMap {

    private String levelPath;
    private String mapFile;

    /**
     * Constructor for class {@code GameMap}. Initializes the maze.
     *
     * @param levelPath Path to the level file.
     */
    public GameMap(String levelPath) {
        this.levelPath = levelPath;
        this.mapFile = levelPath;
        this.map = GameHelper.loadLevelData(levelPath);
        buildWorld();
    }

    private final Map<List<Integer>, Integer> map;
    private static int width;
    private static int height;
    private final List<Wall> walls = new ArrayList<>();
    private final List<Trap> traps = new ArrayList<>();
    private final List<MorphTrap> morphTraps = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Key> keys = new ArrayList<>();
    private final List<Exit> exits = new ArrayList<>();
    private final List<Floor> floors = new ArrayList<>();
    private List<Heart> hearts = new ArrayList<>();
    private List<Boost> boosts = new ArrayList<>();
    private Entrance entrance;


    private GameCharacter player = null;


    private float playerStartX = -1;
    private float playerStartY = -1;

    public static int getWidth() {
        return width;
    }

    public static int getHeight() {
        return height;
    }


    public String getLevelPath() {
        return this.levelPath;
    }


    public List<Wall> getWalls() {
        return walls;
    }

    public GameCharacter getPlayer() {
        return player;
    }


    public void setPlayer(GameCharacter player) {
        this.player = player;
    }


    public float getPlayerStartX() {
        return playerStartX;
    }

    public float getPlayerStartY() {
        return playerStartY;
    }

    public List<Key> getKeys() {
        return keys;
    }

    public List<Trap> getTraps() {
        return traps;
    }


    public List<MorphTrap> getMorphTraps() {
        return morphTraps;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public List<Exit> getExits() {
        return exits;
    }

    public List<Heart> getHearts() {
        return hearts;
    }

    public Entrance getEntrance() {
        return entrance;
    }

    public List<Boost> getBoosts() {
        return boosts;
    }

    public de.tum.cit.fop.maze.world.ExitArrow getExitArrow() {
        return exitArrow;
    }

    private ExitArrow exitArrow;

    private void buildWorld() {
        width = height = 0;
        calculateDimensions();
        List<Floor> emptySpaces = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                List<Integer> coordinates = Arrays.asList(x, y);
                int value = map.getOrDefault(coordinates, -1);
                switch (value) {
                    case 0 -> walls.add(new Wall(x, y));
                    case 1 -> {
                        entrance = new Entrance(x, y);

                        playerStartX = x;
                        playerStartY = y;
                        exitArrow = new ExitArrow();
                    }
                    case 2 -> {
                        Exit e = new Exit(x, y);
                        exits.add(e);
                        walls.add(e);
                        enemies.add(new GhostEnemy(x, y));
                        floors.add(new Floor(x, y));
                    }
                    case 3 -> {
                        traps.add(new Trap(x, y));
                        floors.add(new Floor(x, y));
                    }
                    case 4 -> {
                        enemies.add(new Enemy(x, y));
                        floors.add(new Floor(x, y));
                    }
                    case 5 -> {
                        keys.add(new Key(x, y));
                        floors.add(new Floor(x, y));
                    }
                    case 6 -> {
                        morphTraps.add(new MorphTrap(x, y));
                        floors.add(new Floor(x, y));
                    }
                    default -> {
                        Floor floor = new Floor(x, y);
                        floors.add(floor);
                        emptySpaces.add(floor);
                    }
                }
            }
        }
        hearts = manualPlacements(emptySpaces, CollectableType.Heart, Heart.class);
        boosts = manualPlacements(emptySpaces, CollectableType.BOOST, Boost.class);
    }

    private <T extends MapElement> List<T> manualPlacements(List<Floor> emptySpaces, CollectableType type, Class<T> c) {
        Random random = new Random();
        List<T> placements = new ArrayList<>();
        int elementCount = Math.min(3, emptySpaces.size());
        for (int i = 0; i < elementCount; i++) {
            int index = random.nextInt(emptySpaces.size());
            Floor floor = emptySpaces.remove(index);
            MapElement element;
            switch (type) {
                case Heart -> element = new Heart(floor.getX(), floor.getY());
                case BOOST -> element = new Boost(floor.getX(), floor.getY());
                default -> throw new IllegalArgumentException("Unsupported type: " + type);
            }
            if (c.isInstance(element)) {
                placements.add(c.cast(element));
            }
        }
        return placements;
    }

    private void calculateDimensions() {
        for (List<Integer> coordinates : map.keySet()) {
            int x = coordinates.get(0);
            int y = coordinates.get(1);
            if (x + 1 > width) width = x + 1;
            if (y + 1 > height) height = y + 1;
        }
    }

    public void render(SpriteBatch batch, float delta) {
        floors.forEach(floor -> floor.render(batch));
        walls.forEach(entity -> entity.render(batch, this));
        keys.forEach(entity -> entity.render(batch));
        traps.forEach(entity -> entity.render(batch));
        morphTraps.forEach(entity -> entity.render(batch));
        exits.forEach(entity -> entity.render(batch));
        for (Heart heart : hearts) {
            heart.render(batch, delta);
        }
        boosts.forEach(boost -> boost.render(batch));
        enemies.forEach(entity -> entity.render(batch));
        exitArrow.render(batch);
        if (entrance != null) entrance.render(batch);
        if (player != null) player.render(batch);
    }

    public String getMapFile() {return this.mapFile;}


    public void reloadFrom(String newMapFile) {
        this.mapFile = newMapFile;
        this.levelPath = newMapFile;
        this.map.clear();
        this.map.putAll(GameHelper.loadLevelData(newMapFile));

        buildWorld();
    }

    public void dispose() {
        walls.forEach(Wall::dispose);
        keys.forEach(Key::dispose);
        traps.forEach(Trap::dispose);
        morphTraps.forEach(MorphTrap::dispose); // ← Dispose MorphTrap
        exits.forEach(Exit::dispose);
        enemies.forEach(Enemy::dispose);
        hearts.forEach(Heart::dispose);
        if (entrance != null) entrance.dispose();
        if (player != null) player.dispose();
    }
}