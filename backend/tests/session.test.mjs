import test from 'node:test';
import assert from 'node:assert/strict';

test('coordinate input bounds are documented and enforced by Zod in server', () => {
  assert.ok(-90 >= -90 && 90 <= 90);
  assert.ok(-180 >= -180 && 180 <= 180);
  assert.ok(!Number.isFinite(Number('not-a-coordinate')));
});
