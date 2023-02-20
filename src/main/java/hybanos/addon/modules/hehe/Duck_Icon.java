package hybanos.addon.modules.hehe;

import hybanos.addon.HAHAddon;
import meteordevelopment.meteorclient.systems.modules.Module;

import java.io.InputStream;

public class Duck_Icon extends Module {


    public Duck_Icon() {
        super(HAHAddon.COOKIE, "Duck Icon", "Changes the Windows Icon to a Duck.");
    }

// Made by Cookie
// first time code fr
// update soon (maybe not)

	public void changeIcon(){
            try {
                InputStream stream16 = HAHAddon.class.getClassLoader().getResourceAsStream("assets/hahaddon/textures/icon16.png");
                InputStream stream32 = HAHAddon.class.getClassLoader().getResourceAsStream("assets/hahaddon/textures/icon32.png");

                if (stream16 != null && stream32 != null) mc.getWindow().setIcon(stream16, stream32);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

	}

	public void changeIconDefault(){
            try {
                InputStream stream16 = HAHAddon.class.getClassLoader().getResourceAsStream("assets/hahaddon/textures/defaulticon.png");
                InputStream stream32 = HAHAddon.class.getClassLoader().getResourceAsStream("assets/hahaddon/textures/defaulticon.png");

                if (stream16 != null && stream32 != null) mc.getWindow().setIcon(stream16, stream32);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
	}

    @Override
    public void onActivate() {
		changeIcon();
    }

   @Override
    public void onDeactivate() {
		changeIconDefault();
    }
}	
