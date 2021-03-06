# Hello World

```jsx
import { StateNavigator } from 'navigation';
import { NavigationHandler, NavigationLink, NavigationBackLink } from 'navigation-react';
import { NavigationMotion } from 'navigation-react-mobile';

const stateNavigator = new Navigation.StateNavigator([
  { key: 'hello', route: '' },
  { key: 'world', trackCrumbTrail: true }
]);

const Hello = () => (
  <NavigationLink stateKey="world">
    Hello
  </NavigationLink>
);

const World = () => (
  <NavigationBackLink distance={1}>
    World
  </NavigationBackLink>
);

const { hello, world } = stateNavigator.states;

hello.renderScene = () => <Hello />;
world.renderScene = () => <World />;

stateNavigator.start();

ReactDOM.render(
  <NavigationHandler stateNavigator={stateNavigator}>
    <NavigationMotion
      unmountedStyle={{ translate: 100 }}
      mountedStyle={{ translate: 0 }}
      crumbStyle={{ translate: -10 }}>
      {(style, scene, key) => (
        <div
          key={key}
          style={{ transform: `translate(${style.translate}%)` }}>
          {scene}
        </div>
      )}
    </NavigationMotion>
  </NavigationHandler>,
  document.getElementById('root')
);
```