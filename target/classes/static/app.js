const { useEffect, useRef, useState } = React;
const { createRoot } = ReactDOM;

const api = async (url, options = {}) => {
    const response = await fetch(url, options);
    
    // Handle 204 No Content
    if (response.status === 204) {
        return null;
    }
    
    // Check content type
    const contentType = response.headers.get('content-type');
    
    if (!contentType || !contentType.includes('application/json')) {
        // If not JSON, check if we got an error response
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        // Return text if not JSON
        const text = await response.text();
        return text || null;
    }
    
    try {
        const result = await response.json();
        if (!response.ok) {
            throw new Error(result?.message || `HTTP ${response.status}: ${response.statusText}`);
        }
        return result;
    } catch (jsonError) {
        // If JSON parsing fails but response was OK, return empty
        if (response.ok) {
            return null;
        }
        throw new Error(`Failed to parse JSON: ${jsonError.message}`);
    }
};

const clampValue = value => Math.max(0, Math.min(100, value));

function StatusDot({ device }) {
    return React.createElement('span', {
        className: `status-dot ${device.active ? device.state.toLowerCase() : 'disabled'}`,
        title: device.active ? `Device ${device.state}` : 'Device deactivated',
        'aria-label': device.active ? device.state : 'Deactivated'
    });
}

function SwitchControl({ device, busy, onChange }) {
    return React.createElement('button', {
        className: `switch-control ${device.state === 'ON' ? 'on' : 'off'}`,
        role: 'switch',
        'aria-checked': device.state === 'ON',
        'aria-label': `Turn ${device.name} ${device.state === 'ON' ? 'off' : 'on'}`,
        disabled: busy || !device.active,
        onClick: () => onChange(device.state === 'ON' ? 'OFF' : 'ON', device.state === 'ON' ? 0 : 100)
    }, React.createElement('span', { className: 'switch-track' }, React.createElement('span', { className: 'switch-thumb' })));
}

function SliderControl({ device, busy, onChange }) {
    const value = device.value || 0;
    const sliderRef = useRef(null);
    const changeByWheel = event => {
        if (!busy && device.active && device.state === 'ON') {
            event.preventDefault();
            const next = clampValue(value + (event.deltaY < 0 ? 5 : -5));
            onChange(next > 0 ? 'ON' : 'OFF', next);
        }
    };
    useEffect(() => {
        const element = sliderRef.current;
        element.addEventListener('wheel', changeByWheel, { passive: false });
        return () => element.removeEventListener('wheel', changeByWheel);
    });
    return React.createElement('div', { className: 'slider-wrap', ref: sliderRef },
        React.createElement('button', { type: 'button', className: `slider-indicator ${device.state === 'ON' ? 'on' : 'off'}`, 'aria-label': `Toggle ${device.name}`, disabled: busy || !device.active, onClick: () => onChange(device.state === 'ON' ? 'OFF' : 'ON', device.state === 'ON' ? 0 : value || 100) }, React.createElement('strong', null, `${value}%`)),
        React.createElement('label', { className: 'slider-control' },
            React.createElement('span', null, 'Intensity'),
            React.createElement('output', null, `${value}%`),
            React.createElement('input', { type: 'range', min: 0, max: 100, value, disabled: busy || !device.active, 'aria-label': `Adjust ${device.name}`, onChange: event => { const next = Number(event.target.value); onChange(next > 0 ? 'ON' : 'OFF', next); } })
        )
    );
}

function RgbControl({ device, busy, onChange }) {
    const [color, setColor] = useState({ red: device.red || 0, green: device.green || 0, blue: device.blue || 0 });
    const rgb = { red: color.red, green: color.green, blue: color.blue };
    const wheelRef = useRef(null);
    useEffect(() => setColor({ red: device.red || 0, green: device.green || 0, blue: device.blue || 0 }), [device.red, device.green, device.blue]);
    const update = (channel, next) => {
        const updated = { ...color, [channel]: next };
        setColor(updated);
        onChange(Object.values(updated).some(value => value > 0) ? 'ON' : 'OFF', updated);
    };
    const changeByWheel = event => {
        if (!busy && device.active && device.state === 'ON') {
            event.preventDefault();
            const next = clampValue((device.value || 0) + (event.deltaY < 0 ? 5 : -5));
            onChange(next > 0 ? 'ON' : 'OFF', { ...rgb, red: next, green: next, blue: next });
        }
    };
    useEffect(() => {
        const element = wheelRef.current;
        element.addEventListener('wheel', changeByWheel, { passive: false });
        return () => element.removeEventListener('wheel', changeByWheel);
    });
    const hex = `#${[color.red, color.green, color.blue].map(value => Number(value).toString(16).padStart(2, '0')).join('')}`;
    return React.createElement('div', { className: 'rgb-wrap', ref: wheelRef },
        React.createElement('button', { type: 'button', className: `rgb-preview ${device.state === 'ON' ? 'on' : 'off'}`, style: { backgroundColor: device.state === 'ON' ? hex : '#68716d' }, disabled: busy || !device.active, onClick: () => onChange(device.state === 'ON' ? 'OFF' : 'ON', device.state === 'ON' ? 0 : 100) }, hex.toUpperCase()),
        React.createElement('div', { className: 'rgb-sliders' }, ['red', 'green', 'blue'].map(channel => React.createElement('label', { key: channel }, channel.toUpperCase(), React.createElement('output', null, color[channel]), React.createElement('input', { type: 'range', min: 0, max: 255, value: color[channel], disabled: busy || !device.active, onChange: event => update(channel, Number(event.target.value)), 'aria-label': `${channel} channel` })))),
        React.createElement('small', { className: 'wheel-hint' }, device.state === 'ON' ? 'Scroll to adjust brightness' : 'Click the color to enable scrolling')
    );
}

function SensorControl({ device }) {
    return React.createElement('div', { className: 'sensor-reading' },
        React.createElement('strong', null, `${device.value || 0}`),
        React.createElement('span', null, device.sensorType || 'sensor'),
        React.createElement('small', null, 'Used as an automation trigger')
    );
}

function DeviceCard({ device, onState, onLifecycle, onRemove }) {
    const [busy, setBusy] = useState(false);
    const updateState = async (state, value) => { setBusy(true); await onState(device.id, state, value); setBusy(false); };
    const control = device.controlType === 'SWITCH'
        ? React.createElement(SwitchControl, { device, busy, onChange: updateState })
        : device.controlType === 'RGB'
            ? React.createElement(RgbControl, { device, busy, onChange: (state, value) => {
                if (typeof value === 'object') onState(device.id, state, value);
                else onState(device.id, state, { red: device.red, green: device.green, blue: device.blue });
            } })
            : device.controlType === 'SENSOR'
                ? React.createElement(SensorControl, { device })
        : React.createElement(SliderControl, { device, busy, onChange: updateState });
    return React.createElement('article', { className: `device-card ${device.active ? '' : 'inactive'}` },
        React.createElement('div', { className: 'card-heading' }, React.createElement('div', null, React.createElement('p', { className: 'device-type' }, device.controlType), React.createElement('h3', null, device.name)), React.createElement(StatusDot, { device })),
        React.createElement('div', { className: 'control-stage' }, control),
        React.createElement('div', { className: 'card-footer' },
            React.createElement('button', { className: 'text-action', onClick: () => onLifecycle(device.id, device.active ? 'deactivate' : 'activate') }, device.active ? 'Deactivate' : 'Activate'),
            React.createElement('button', { className: 'text-action danger', onClick: () => onRemove(device.id) }, 'Remove')
        )
    );
}

function AutomationView({ devices, setMessage }) {
    const [rules, setRules] = useState(() => JSON.parse(localStorage.getItem('home-automation-rules') || '[]'));
    const [triggerType, setTriggerType] = useState('motion');
    const saveRule = event => {
        event.preventDefault();
        const form = new FormData(event.currentTarget);
        const triggerDevice = devices.find(device => String(device.id) === form.get('triggerDevice'));
        const actionDevice = devices.find(device => String(device.id) === form.get('actionDevice'));
        if (!triggerDevice || !actionDevice) return;
        const next = [...rules, { triggerDevice: triggerDevice.name, triggerLabel: form.get('triggerType'), threshold: form.get('threshold'), actionDevice: actionDevice.name, actionType: form.get('actionType') }];
        setRules(next); localStorage.setItem('home-automation-rules', JSON.stringify(next)); setMessage('Automation saved');
    };
    const removeRule = index => { const next = rules.filter((_, itemIndex) => itemIndex !== index); setRules(next); localStorage.setItem('home-automation-rules', JSON.stringify(next)); };
    const options = devices.map(device => React.createElement('option', { key: device.id, value: device.id }, device.name));
    return React.createElement('section', { className: 'automation-view' },
        React.createElement('div', { className: 'automation-heading' }, React.createElement('div', null, React.createElement('p', { className: 'section-label' }, 'AUTOMATION BUILDER'), React.createElement('h2', null, 'Make your home respond'), React.createElement('p', { className: 'intro' }, 'Build a rule in three simple steps.')), React.createElement('span', { className: 'automation-mark' }, 'RULES / 03')),
        React.createElement('form', { className: 'automation-form', onSubmit: saveRule },
            React.createElement('fieldset', null, React.createElement('legend', null, '01 ', React.createElement('span', null, 'Choose device')), React.createElement('label', null, 'Sensor or device', React.createElement('select', { name: 'triggerDevice', required: true }, options))),
            React.createElement('fieldset', null, React.createElement('legend', null, '02 ', React.createElement('span', null, 'Set trigger')), React.createElement('label', null, 'When', React.createElement('select', { name: 'triggerType', value: triggerType, onChange: event => setTriggerType(event.target.value) }, React.createElement('option', { value: 'motion' }, 'Motion detected'), React.createElement('option', { value: 'Temperature rises above' }, 'Temperature rises above'), React.createElement('option', { value: 'Device turns on' }, 'Device turns on'))), triggerType === 'Temperature rises above' && React.createElement('label', null, 'Value', React.createElement('input', { name: 'threshold', type: 'number', defaultValue: '30', min: '0', max: '100' }))),
            React.createElement('fieldset', null, React.createElement('legend', null, '03 ', React.createElement('span', null, 'Define action')), React.createElement('label', null, 'Control device', React.createElement('select', { name: 'actionDevice', required: true }, options)), React.createElement('label', null, 'Action', React.createElement('select', { name: 'actionType' }, React.createElement('option', { value: 'on' }, 'Turn on'), React.createElement('option', { value: 'off' }, 'Turn off'))))),
            React.createElement('button', { className: 'primary save-rule', type: 'submit', disabled: !devices.length }, 'Save automation')
        ),
        React.createElement('section', { className: 'rules-section' }, React.createElement('div', { className: 'rules-title' }, React.createElement('p', { className: 'section-label' }, 'YOUR RULES'), React.createElement('span', null, `${rules.length} active`)), rules.map((rule, index) => React.createElement('article', { className: 'automation-rule', key: `${rule.triggerDevice}-${index}` }, React.createElement('div', { className: 'rule-block trigger-block' }, React.createElement('small', null, 'WHEN'), React.createElement('strong', null, rule.triggerDevice), React.createElement('span', null, rule.triggerLabel)), React.createElement('span', { className: 'rule-arrow' }, '->'), React.createElement('div', { className: 'rule-block action-block' }, React.createElement('small', null, 'THEN'), React.createElement('strong', null, rule.actionType === 'on' ? 'Turn on' : 'Turn off'), React.createElement('span', null, rule.actionDevice)), React.createElement('button', { className: 'rule-remove', type: 'button', onClick: () => removeRule(index), 'aria-label': 'Remove automation' }, 'x')), !rules.length && React.createElement('p', { className: 'empty-state' }, 'No automations yet. Build your first rule above.'))
    );
}

function AutomationViewFixed({ devices, setMessage }) {
    const [savedRules, setSavedRules] = useState(() => JSON.parse(localStorage.getItem('home-automation-rules') || '[]'));
    const [triggerType, setTriggerType] = useState('value-rises');
    const saveRule = event => {
        event.preventDefault();
        const form = new FormData(event.currentTarget);
        const triggerDevice = devices.find(device => String(device.id) === form.get('triggerDevice'));
        const actionDevice = devices.find(device => String(device.id) === form.get('actionDevice'));
        if (!triggerDevice || !actionDevice) return;
        const next = [...savedRules, { triggerDevice: triggerDevice.name, triggerLabel: form.get('triggerType'), threshold: form.get('threshold'), actionDevice: actionDevice.name, actionType: form.get('actionType') }];
        setSavedRules(next);
        localStorage.setItem('home-automation-rules', JSON.stringify(next));
        setMessage('Automation saved');
    };
    const sensors = devices.filter(device => device.controlType === 'SENSOR');
    const actuators = devices.filter(device => device.controlType !== 'SENSOR');
    const triggerOptions = sensors.length
        ? [React.createElement('option', { key: 'rises', value: 'value-rises' }, 'Value rises above'), React.createElement('option', { key: 'falls', value: 'value-falls' }, 'Value falls below'), React.createElement('option', { key: 'changes', value: 'value-changes' }, 'Value changes')]
        : [React.createElement('option', { key: 'on', value: 'on' }, 'Device turns on')];
    const sensorOptions = sensors.map(device => React.createElement('option', { key: device.id, value: device.id }, `${device.name} (${device.sensorType || 'sensor'})`));
    const actuatorOptions = actuators.map(device => React.createElement('option', { key: device.id, value: device.id }, device.name));
    const heading = React.createElement('div', { className: 'automation-heading' },
        React.createElement('div', null, React.createElement('p', { className: 'section-label' }, 'AUTOMATION BUILDER'), React.createElement('h2', null, 'Make your home respond'), React.createElement('p', { className: 'intro' }, 'Build a rule in three simple steps.')),
        React.createElement('span', { className: 'automation-mark' }, 'RULES / 03')
    );
    const form = React.createElement('form', { className: 'automation-form', onSubmit: saveRule },
        React.createElement('fieldset', null, React.createElement('legend', null, '01 ', React.createElement('span', null, 'Choose sensor')), React.createElement('label', null, 'Sensor', React.createElement('select', { name: 'triggerDevice', required: true }, sensorOptions))),
        React.createElement('fieldset', null, React.createElement('legend', null, '02 ', React.createElement('span', null, 'Set trigger')), React.createElement('label', null, 'When', React.createElement('select', { name: 'triggerType', value: triggerType, onChange: event => setTriggerType(event.target.value) }, triggerOptions)), React.createElement('label', null, 'Value', React.createElement('input', { name: 'threshold', type: 'number', defaultValue: '30', min: '0', max: '255' }))),
        React.createElement('fieldset', null, React.createElement('legend', null, '03 ', React.createElement('span', null, 'Define action')), React.createElement('label', null, 'Control device', React.createElement('select', { name: 'actionDevice', required: true }, actuatorOptions)), React.createElement('label', null, 'Action', React.createElement('select', { name: 'actionType' }, React.createElement('option', { value: 'on' }, 'Turn on'), React.createElement('option', { value: 'off' }, 'Turn off')))),
        React.createElement('button', { className: 'primary save-rule', type: 'submit', disabled: !sensors.length || !actuators.length }, 'Save automation')
    );
    const ruleSection = React.createElement('section', { className: 'rules-section' },
        React.createElement('div', { className: 'rules-title' }, React.createElement('p', { className: 'section-label' }, 'YOUR RULES'), React.createElement('span', null, `${savedRules.length} active`)),
        savedRules.map((rule, index) => React.createElement('article', { className: 'automation-rule', key: `${rule.triggerDevice}-${index}` }, React.createElement('div', { className: 'rule-block trigger-block' }, React.createElement('small', null, 'WHEN'), React.createElement('strong', null, rule.triggerDevice), React.createElement('span', null, rule.triggerLabel)), React.createElement('span', { className: 'rule-arrow' }, '->'), React.createElement('div', { className: 'rule-block action-block' }, React.createElement('small', null, 'THEN'), React.createElement('strong', null, rule.actionType === 'on' ? 'Turn on' : 'Turn off'), React.createElement('span', null, rule.actionDevice)), React.createElement('button', { className: 'rule-remove', type: 'button', onClick: () => { const next = savedRules.filter((_, itemIndex) => itemIndex !== index); setSavedRules(next); localStorage.setItem('home-automation-rules', JSON.stringify(next)); }, 'aria-label': 'Remove automation' }, 'x'))),
        !savedRules.length && React.createElement('p', { className: 'empty-state' }, 'No automations yet. Build your first rule above.')
    );
    return React.createElement('section', { className: 'automation-view' }, heading, form, ruleSection);
}

function App() {
    const [devices, setDevices] = useState([]);
    const [filter, setFilter] = useState('all');
    const [view, setView] = useState('home');
    const [message, setMessage] = useState('Loading device states...');
    const [error, setError] = useState(false);
    const loadDevices = async () => { try { setDevices(await api('/devices')); setError(false); setMessage('Ready'); } catch (requestError) { setError(true); setMessage(requestError.message); } };
    useEffect(() => { loadDevices(); }, []);
    const updateState = async (id, state, value) => {
        try {
            const payload = typeof value === 'object' ? { state, ...value } : { state, value };
            const result = await api(`/device/${id}/state`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) });
            setMessage(result.message); await loadDevices();
        } catch (requestError) { setError(true); setMessage(requestError.message); }
    };
    const lifecycle = async (id, action) => { try { await api(`/device/${id}/${action}`, { method: 'POST' }); await loadDevices(); setMessage(`Device ${action}d`); } catch (requestError) { setError(true); setMessage(requestError.message); } };
    const remove = async id => { if (!window.confirm('Remove this device?')) return; try { await api(`/device/${id}`, { method: 'DELETE' }); await loadDevices(); setMessage('Device removed'); } catch (requestError) { setError(true); setMessage(requestError.message); } };
    const addDevice = async event => { event.preventDefault(); const formElement = event.currentTarget; const form = new FormData(formElement); try { const result = await api('/device', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ name: form.get('name'), controlType: form.get('controlType'), sensorType: form.get('sensorType') }) }); formElement.reset(); await loadDevices(); setMessage(`${result.name} added to the simulator`); } catch (requestError) { setError(true); setMessage(requestError.message); } };
    const visibleDevices = devices.filter(device => filter === 'all' || (filter === 'deactivated' && !device.active) || (filter === 'on' && device.active && device.state === 'ON') || (filter === 'off' && device.active && device.state === 'OFF'));
    return React.createElement('main', { className: 'dashboard' },
        React.createElement('header', { className: 'hero' }, React.createElement('div', null, React.createElement('p', { className: 'eyebrow' }, 'VIRTUAL HOME / LIVE CONSOLE'), React.createElement('h1', null, 'Home Control'), React.createElement('p', { className: 'intro' }, 'A quieter, clearer way to run your rooms.')), React.createElement('div', { className: 'connection' }, React.createElement('span', { className: 'pulse' }), ' Simulator connected')),
        React.createElement('nav', { className: 'tabs', 'aria-label': 'Main navigation' }, ['home', 'automation'].map(item => React.createElement('button', { className: `tab ${view === item ? 'active' : ''}`, key: item, onClick: () => setView(item) }, item === 'home' ? 'Home' : 'Automation'))),
        view === 'home' ? React.createElement(React.Fragment, null,
            React.createElement('section', { className: 'summary' }, React.createElement('div', null, React.createElement('span', null, devices.length), React.createElement('small', null, 'Total devices')), React.createElement('div', null, React.createElement('span', null, devices.filter(device => device.active && device.state === 'ON').length), React.createElement('small', null, 'Currently on')), React.createElement('div', null, React.createElement('span', null, devices.filter(device => !device.active).length), React.createElement('small', null, 'Deactivated'))),
            React.createElement('section', { className: 'workspace-bar' }, React.createElement('div', null, React.createElement('p', { className: 'section-label' }, 'DEVICE MONITOR'), React.createElement('h2', null, 'All devices')), React.createElement('label', { className: 'filter' }, 'Show', React.createElement('select', { value: filter, onChange: event => setFilter(event.target.value) }, React.createElement('option', { value: 'all' }, 'All devices'), React.createElement('option', { value: 'on' }, 'On'), React.createElement('option', { value: 'off' }, 'Off'), React.createElement('option', { value: 'deactivated' }, 'Deactivated')))),
            React.createElement('section', { className: 'device-grid' }, visibleDevices.map(device => React.createElement(DeviceCard, { key: device.id, device, onState: updateState, onLifecycle: lifecycle, onRemove: remove }))), !visibleDevices.length && React.createElement('p', { className: 'empty-state' }, 'No devices match this filter.'),
            React.createElement('section', { className: 'add-panel' }, React.createElement('div', null, React.createElement('p', { className: 'section-label' }, 'VIRTUAL DEVICE LAB'), React.createElement('h2', null, 'Add a device'), React.createElement('p', null, 'Create a fake device to test commands before hardware arrives.')), React.createElement('form', { onSubmit: addDevice }, React.createElement('label', null, 'Device name', React.createElement('input', { name: 'name', required: true, placeholder: 'Bedroom lamp' })), React.createElement('label', null, 'Control type', React.createElement('select', { name: 'controlType', defaultValue: 'SWITCH' }, React.createElement('option', { value: 'SWITCH' }, 'Switch'), React.createElement('option', { value: 'SLIDER' }, 'Slider'), React.createElement('option', { value: 'RGB' }, 'RGB color'), React.createElement('option', { value: 'SENSOR' }, 'Sensor'))), React.createElement('label', null, 'Sensor kind', React.createElement('select', { name: 'sensorType', defaultValue: 'light' }, React.createElement('option', { value: 'light' }, 'Light'), React.createElement('option', { value: 'wind' }, 'Wind'), React.createElement('option', { value: 'temperature' }, 'Temperature'), React.createElement('option', { value: 'motion' }, 'Motion'))), React.createElement('button', { className: 'primary', type: 'submit' }, 'Add device')))
        ) : React.createElement(AutomationViewFixed, { devices, setMessage }),
        React.createElement('p', { className: `message ${error ? 'error' : ''}`, role: 'status', 'aria-live': 'polite' }, message)
    );
}

createRoot(document.querySelector('#root')).render(React.createElement(App));
