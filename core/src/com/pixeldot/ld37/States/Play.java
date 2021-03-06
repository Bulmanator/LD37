package com.pixeldot.ld37.States;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.pixeldot.ld37.Entities.Player;
import com.pixeldot.ld37.Entities.WorldObject;
import com.pixeldot.ld37.Entities.WorldObjects.Block;
import com.pixeldot.ld37.Entities.WorldObjects.Box;
import com.pixeldot.ld37.Entities.WorldObjects.Switch;
import com.pixeldot.ld37.Utilities.Animation;
import com.pixeldot.ld37.Utilities.CollisionListener;
import com.pixeldot.ld37.Utilities.ContentManager;
import com.pixeldot.ld37.Utilities.GameStateManager;

import java.util.ArrayList;

import static com.pixeldot.ld37.Game.*;

public class Play extends State {
    // TODO Level loading
    // TODO make a handle input method
    // TODO better triggers
    // TODO buttons
    // TODO changing level animations
    // TODO Aisu walking around the level
    // TODO doors
    // TODO locks?

    private Texture background, backgroundRice;
    private Texture backgroundLeft, backgroundRight;

    private Player player;

    private Body boxBody;
    private Animation boxAnim;
    private BodyDef boxDef;
    private FixtureDef boxFDef;

    private BitmapFont font = new BitmapFont(true);

    private CollisionListener contactListener;

    private ArrayList<WorldObject> worldObjects;
    private boolean jointMade;
    private Joint joint;

    public Play(GameStateManager gsm) {
        super(gsm);

        world.setContactListener(contactListener = new CollisionListener());
        worldObjects = new ArrayList<>();

        ContentManager.loadTexture("PlayerRun", "Character/spritesheetSmol.png");
        ContentManager.loadTexture("PlayerWall", "Character/pushSpriteSheet.png");
        ContentManager.loadTexture("PlayerIdle", "Character/idleSpriteSheet.png");
        ContentManager.loadTexture("Rice", "Materials/SpringSummerAutumnBackground.png");
        ContentManager.loadTexture("Background", "Materials/Background.png");
        ContentManager.loadTexture("LeftAutumn", "Side Panels/autumnLeftSlide.png");
        ContentManager.loadTexture("RightAutumn", "Side Panels/autumnRightSlide.png");


        ContentManager.loadTexture("Brick", "Materials/brickTexture.png");


        background = ContentManager.getTexture("Background");
        backgroundRice = ContentManager.getTexture("Rice");
        backgroundLeft = ContentManager.getTexture("LeftAutumn");
        backgroundRight = ContentManager.getTexture("RightAutumn");


        createRoom();
        createPlayer();
    }

    public void update(float dt) {

        if (Gdx.input.isTouched()) {
            mouse.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            box2DCam.unproject(mouse);

            player.getBody().setTransform(mouse.x, mouse.y, 0);
        }
        for(WorldObject wo: worldObjects) {
            wo.update(dt);
            if(wo instanceof Box){
                /*if(((Box) wo).isBeingPulled()) {
                    if (!jointMade) {
                        DistanceJointDef def = new DistanceJointDef();
                        def.type = JointDef.JointType.DistanceJoint;
                        def.bodyA = player.getBody();
                        def.bodyB = ((Box) wo).getBody();
                        def.collideConnected = true;
                        def.length = 78/PPM;
                        joint = world.createJoint(def);
                        jointMade = true;
                    }
                }
                else if(jointMade)
                {
                    jointMade=false;
                    world.destroyJoint(joint);
                }*/
            }
        }

        world.step(dt, 6, 2);
        player.update(dt);
    }

    public void render() {

        batch.setProjectionMatrix(camera.combined);
        renderer.setProjectionMatrix(camera.combined);
        Animation back = new Animation("",ContentManager.getTexture("Rice"),1,1);
        back.setFlipY(false);
        back.setTargetWidth(1280);
        back.setTargetHeight(720);

        batch.begin();
        batch.draw(backgroundRice, 0, 0, WIDTH, HEIGHT,
                0, 0, backgroundRice.getWidth(), backgroundRice.getHeight(), false, true);

        batch.draw(background, 0, 0, WIDTH, HEIGHT,
                0, 0, background.getWidth(), background.getHeight(), false, true);

        batch.draw(backgroundLeft, 0, 0, backgroundLeft.getWidth() / 1.5f, HEIGHT,
                0, 0, backgroundLeft.getWidth(), backgroundLeft.getHeight(), false, true);

        batch.draw(backgroundRight, (1161 + 378) / 1.5f, 0, backgroundRight.getWidth() / 1.5f, HEIGHT,
                0, 0, backgroundRight.getWidth(), backgroundRight.getHeight(), false, true);

        player.render(batch);
        for(WorldObject wo: worldObjects)
            wo.render(batch);
        batch.end();
        debugRenderer.render(world, box2DCam.combined);
    }

    public void dispose() {}

    private void createRoom() {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(WIDTH / PPM, 20 / PPM, new Vector2(-(WIDTH / 2) / PPM, ((HEIGHT / 2) - 10) / PPM), 0);

        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.StaticBody;
        bdef.position.set((WIDTH / 2) / PPM, (HEIGHT / 2) / PPM);

        Body room = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.friction = 0.2f;
        room.createFixture(fdef).setUserData("Floor");

        shape.setAsBox((252+24) / PPM, HEIGHT / PPM, new Vector2(((-WIDTH / 2) + 10) / PPM, 0), 0); // left wall
        room.createFixture(fdef).setUserData("Wall");


        shape.setAsBox((252+24) / PPM, HEIGHT / PPM, new Vector2(((WIDTH / 2) - 10) / PPM, 0), 0); // right wall
        room.createFixture(fdef).setUserData("Wall");

        shape.setAsBox(WIDTH / PPM, 40 / PPM, new Vector2(0, ((-HEIGHT / 2) - 10) / PPM), 0); //ceiling
        room.createFixture(fdef).setUserData("Wall");

      //  Block block = new Block(world, new Vector2(700 / PPM, (HEIGHT - 50) / PPM), "Brick");
     //   Switch aSwitch = new Switch(world, new Vector2(400 / PPM, (HEIGHT - 50) / PPM), block);

       // worldObjects.add(block);
       // worldObjects.add(aSwitch);

        shape.dispose();
    }
    private void createPlayer() {
        BodyDef playerDef = new BodyDef();
        playerDef.position.set(100 / PPM, (HEIGHT - 100) / PPM);
        playerDef.type = BodyDef.BodyType.DynamicBody;
        playerDef.active = true;
        playerDef.allowSleep = true;
        playerDef.fixedRotation = true;
        playerDef.linearDamping = 2;

        Body playerBody = world.createBody(playerDef);

        FixtureDef playerFDef = new FixtureDef();
        playerFDef.density = 1;
        playerFDef.friction = 0.1f;

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(30 / PPM, 42 / PPM);
        playerFDef.shape = shape;
        playerBody.createFixture(playerFDef).setUserData("PlayerMain");

        shape.setAsBox(16 / PPM, 8 / PPM, new Vector2(0, 49 / PPM), 0);
        playerFDef.isSensor = true;

        playerBody.createFixture(playerFDef).setUserData("Foot");

        shape.setAsBox(8 / PPM, 16 / PPM, new Vector2(30/PPM, 0 / PPM), 0);
        playerFDef.isSensor = true;


        Animation a = new Animation("Run", ContentManager.getTexture("PlayerRun"), 2, 4);
        a.setTargetWidth(60);
        a.setTargetHeight(90);

        player = new Player(playerBody, world, a);

        a = new Animation("SquishFace",ContentManager.getTexture("PlayerWall"), 2,4);
        a.setEndFrame(6);
        a.setTargetWidth(60);
        a.setTargetHeight(90);
        player.addAnimation(a);

        a = new Animation("Idle",ContentManager.getTexture("PlayerIdle"), 6,4);
        a.setTargetWidth(60);
        a.setTargetHeight(90);
        player.addAnimation(a);

        shape.dispose();

        boxDef = new BodyDef();
        boxDef.type = BodyDef.BodyType.DynamicBody;
        boxDef.position.set(200 / PPM, (HEIGHT - 300) / PPM);

        boxBody = world.createBody(boxDef);

        //worldObjects.add(new Box(world,"Brick",new Vector2(150/PPM,50/PPM),contactListener));
      //  worldObjects.add(new Box(world,"Brick",new Vector2(500/PPM,50/PPM),contactListener));
    }
}
