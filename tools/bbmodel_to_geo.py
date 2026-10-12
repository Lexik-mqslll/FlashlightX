"""Convert the flashlight Blockbench project into GeckoLib geometry and texture.

Usage:  python tools/bbmodel_to_geo.py <path to .bbmodel>

The project is authored in the `java_block` format, which GeckoLib cannot read.
Coordinates are mapped with (jx, jy, jz) -> (8 - jx, jy - 8.16, jz - 8), which
places the model exactly where vanilla would draw the java model, so the display
transforms set in Blockbench carry over unchanged. GeckoLib mirrors X itself, so
the mirror in the formula cancels out and only east/west swap names. The 8.16
offset accounts for the extra 0.01 block in GeckoLib's preRender translate.

Cube rotation is passed through unchanged: GeckoLib negates the X and Y angles
during baking and uses Z as written.

Folders become bones; cubes outside any folder belong to the body.
"""
import base64
import json
import math
import os
import sys

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..',
                    'src', 'main', 'resources', 'assets', 'flashlightx')
GEO_OUT = os.path.join(ROOT, 'geo', 'flashlight.geo.json')
TEX_OUT = os.path.join(ROOT, 'textures', 'item', 'flashlight_texture.png')
ANIM_OUT = os.path.join(ROOT, 'animations', 'flashlight.animation.json')
MODEL_OUT = os.path.join(ROOT, 'models', 'item', 'flashlight.json')

SWAP = {'east': 'west', 'west': 'east'}
LENS, BUTTON, BODY = 'Flashlight_Lens', 'Flashlight_Button', 'Flashlight_Body'


Y_OFFSET = 8.16          # 8 px of centring + GeckoLib's extra 0.01 block


def point(p):
    return [round(8 - p[0], 4), round(p[1] - Y_OFFSET, 4), round(p[2] - 8, 4)]


def convert(el):
    f, t = el['from'], el['to']
    cube = {'origin': [round(8 - t[0], 4), round(f[1] - Y_OFFSET, 4), round(f[2] - 8, 4)],
            'size': [round(t[i] - f[i], 4) for i in range(3)]}
    rot = el.get('rotation')
    if rot and any(rot):
        cube['pivot'] = point(el['origin'])
        # GeckoLib negates the X and Y angles when baking, so pre-negate them;
        # the Z angle it uses as written.
        cube['rotation'] = [round(-rot[0], 4), round(-rot[1], 4), round(rot[2], 4)]

    uv = {}
    for face, data in el['faces'].items():
        u1, v1, u2, v2 = data['uv']
        if u1 == u2 or v1 == v2:
            continue
        uv[SWAP.get(face, face)] = {
            'uv': [round(min(u1, u2), 4), round(min(v1, v2), 4)],
            'uv_size': [round(abs(u2 - u1), 4), round(abs(v2 - v1), 4)]}
    cube['uv'] = uv
    return cube


def rendered_corners(c):
    """The eight corners of a cube as GeckoLib draws them.

    X is mirrored and the angles are applied as (-x, -y, +z), in the order
    Z, Y, X. Rotated cubes only reach their final place once rotated, so bounds
    must be measured here rather than from origin/size.
    """
    o, s = c['origin'], c['size']
    pts = [[-(o[0] + (s[0] if b & 1 else 0)), o[1] + (s[1] if b >> 1 & 1 else 0),
            o[2] + (s[2] if b >> 2 & 1 else 0)] for b in range(8)]
    if 'rotation' not in c:
        return pts
    pv = [-c['pivot'][0], c['pivot'][1], c['pivot'][2]]
    rx, ry, rz = (math.radians(-c['rotation'][0]), math.radians(-c['rotation'][1]),
                  math.radians(c['rotation'][2]))
    out = []
    for p in pts:
        x, y, z = (p[i] - pv[i] for i in range(3))
        x, y = x * math.cos(rz) - y * math.sin(rz), x * math.sin(rz) + y * math.cos(rz)
        x, z = x * math.cos(ry) + z * math.sin(ry), -x * math.sin(ry) + z * math.cos(ry)
        y, z = y * math.cos(rx) - z * math.sin(rx), y * math.sin(rx) + z * math.cos(rx)
        out.append([x + pv[0], y + pv[1], z + pv[2]])
    return out


def model_centre_x(bones):
    """Model centre on X in render space."""
    xs = [p[0] for b in bones[1:] for c in b['cubes'] for p in rendered_corners(c)]
    return (min(xs) + max(xs)) / 2


def group_cubes(node, els, out):
    for child in node.get('children', []):
        if isinstance(child, str):
            out.append(els[child])
        else:
            group_cubes(child, els, out)


def build(src):
    bb = json.load(open(src, encoding='utf-8'))
    assert bb['meta']['model_format'] == 'java_block', bb['meta']['model_format']
    res = bb['resolution']
    els = {e['uuid']: e for e in bb['elements']}

    groups, loose = [], []
    for node in bb['outliner']:
        if isinstance(node, str):
            loose.append(els[node])
        else:
            cubes = []
            group_cubes(node, els, cubes)
            groups.append(cubes)

    assert len(groups) == 3, f'expected lens/button/body folders, got {len(groups)}'
    lens, button, body = groups
    assert len(lens) == 1 and len(button) == 1, 'lens and button should hold one cube each'

    def volume(e):
        return abs((e['to'][0] - e['from'][0]) * (e['to'][1] - e['from'][1])
                   * (e['to'][2] - e['from'][2]))
    assert volume(button[0]) < volume(lens[0]), \
        'second folder is not the button - check the folder order in Blockbench'

    body = body + loose                      # cubes outside any folder are body parts
    bones = [{'name': 'Flashlight_Full', 'pivot': [0, 0, 0]}]
    for name, group in ((LENS, lens), (BUTTON, button), (BODY, body)):
        cubes = [convert(e) for e in group]
        pts = [(c['origin'], c['size']) for c in cubes]
        pivot = [round(sum(o[i] + s[i] / 2 for o, s in pts) / len(pts), 4) for i in range(3)]
        if name == BUTTON:
            pivot[1] = min(o[1] for o, _ in pts)   # hinge at the base
        bones.append({'name': name, 'parent': 'Flashlight_Full', 'pivot': pivot, 'cubes': cubes})

    geo = {'format_version': '1.12.0', 'minecraft:geometry': [{
        'description': {'identifier': 'geometry.flashlight',
                        'texture_width': res['width'], 'texture_height': res['height'],
                        'visible_bounds_width': 2, 'visible_bounds_height': 1.5,
                        'visible_bounds_offset': [0, 0.5, 0]},
        'bones': bones}]}
    with open(GEO_OUT, 'w', encoding='utf-8', newline='\n') as fh:
        fh.write(json.dumps(geo, indent=2))

    with open(TEX_OUT, 'wb') as fh:
        fh.write(base64.b64decode(bb['textures'][0]['source'].split(',', 1)[1]))

    button_height = button[0]['to'][1] - button[0]['from'][1]
    write_animation(button_height)
    centre_x = model_centre_x(bones)
    slots = write_display(bb.get('display') or {}, centre_x)
    return bones, button_height, slots


def rotated_centre_x(rotation, centre_x):
    """X component of the model centre after the display rotation."""
    rx, ry, rz = (math.radians(a) for a in rotation)
    x, y, z = centre_x, 0.0, 0.0
    x, y = x * math.cos(rz) - y * math.sin(rz), x * math.sin(rz) + y * math.cos(rz)
    x, z = x * math.cos(ry) + z * math.sin(ry), -x * math.sin(ry) + z * math.cos(ry)
    return x


def write_display(display, centre_x):
    """Copy the slots set in Blockbench into the item model.

    Values need no adjustment because the geometry is mapped onto vanilla's own
    coordinates. Slots untouched in the project keep their current values.
    """
    if not display:
        return []

    with open(MODEL_OUT, encoding='utf-8') as fh:
        model = json.load(fh)

    written = []
    for slot, t in display.items():
        entry = {'rotation': [round(v, 4) for v in t.get('rotation', [0, 0, 0])],
                 'translation': [round(v, 4) for v in t.get('translation', [0, 0, 0])],
                 'scale': [round(v, 4) for v in t.get('scale', [1, 1, 1])]}
        model['display'][slot] = entry
        written.append(slot)
        # Mirror the right hand onto the left unless that slot was authored too.
        # Vanilla negates translation.x for the left hand, but the model offset
        # from the hand axis does not flip with it, so shift by twice that offset.
        mirror = slot.replace('righthand', 'lefthand')
        if slot.endswith('righthand') and mirror not in display:
            left = {k: list(v) for k, v in entry.items()}
            left['translation'][0] = round(
                entry['translation'][0] + 2 * entry['scale'][0] * rotated_centre_x(
                    entry['rotation'], centre_x), 4)
            model['display'][mirror] = left
            written.append(mirror + ' (mirrored)')

    with open(MODEL_OUT, 'w', encoding='utf-8', newline='\n') as fh:
        fh.write(json.dumps(model, indent=2))
    return written


def write_animation(button_height):
    """Writes the press animations, scaled to the height of the button cube."""
    press = -round(button_height * 0.64, 3)
    held = -round(button_height * 0.43, 3)

    def hold(value):
        return {'loop': 'hold_on_last_frame', 'animation_length': 0.0417,
                'bones': {BUTTON: {'position': {'0.0': {'vector': [0, value, 0]},
                                                '0.0417': {'vector': [0, value, 0]}}}}}

    def dip(start, end):
        return {'loop': 'hold_on_last_frame', 'animation_length': 0.25,
                'bones': {BUTTON: {'position': {
                    '0.0': {'vector': [0, start, 0]},
                    '0.125': {'vector': [0, press, 0], 'easing': 'easeOutCubic'},
                    '0.25': {'vector': [0, end, 0], 'easing': 'easeOutCubic'}}}}}

    anim = {'format_version': '1.8.0', 'animations': {
        'flashlight_off_idle': hold(0), 'flashlight_on': dip(0, held),
        'flashlight_on_idle': hold(held), 'flashlight_off': dip(held, 0)}}
    with open(ANIM_OUT, 'w', encoding='utf-8', newline='\n') as fh:
        fh.write(json.dumps(anim, indent=2))
    return press, held


if __name__ == '__main__':
    bones, bh, slots = build(sys.argv[1])

    rendered = rendered_corners

    for b in bones[1:]:
        p = [q for c in b['cubes'] for q in rendered(c)]
        print(f"{b['name']:18} {len(b['cubes']):2d} cubes  "
              f"x {min(v[0] for v in p):6.2f}..{max(v[0] for v in p):5.2f}  "
              f"y {min(v[1] for v in p):5.2f}..{max(v[1] for v in p):5.2f}  "
              f"z {min(v[2] for v in p):6.2f}..{max(v[2] for v in p):5.2f}")
    allp = [q for b in bones[1:] for c in b['cubes'] for q in rendered(c)]
    print('TOTAL                 x %6.2f..%5.2f  y %5.2f..%5.2f  z %6.2f..%5.2f' % (
        min(v[0] for v in allp), max(v[0] for v in allp), min(v[1] for v in allp),
        max(v[1] for v in allp), min(v[2] for v in allp), max(v[2] for v in allp)))
    print('button height', bh, '-> press/held written to the animation')
    print('display slots taken from the project:', ', '.join(slots) if slots else 'none')
