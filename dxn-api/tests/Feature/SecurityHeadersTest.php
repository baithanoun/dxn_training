<?php

namespace Tests\Feature;

use App\Http\Middleware\SecurityHeaders;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Http\Request;
use Tests\TestCase;

class SecurityHeadersTest extends TestCase
{
    use RefreshDatabase;

    public function test_api_response_has_nosniff_and_frame_deny(): void
    {
        $response = $this->getJson('/api/app/config');

        $response->assertOk();
        $this->assertSame('nosniff', $response->headers->get('X-Content-Type-Options'));
        $this->assertSame('DENY', $response->headers->get('X-Frame-Options'));
        $this->assertSame('strict-origin-when-cross-origin', $response->headers->get('Referrer-Policy'));
        $this->assertSame('camera=(), microphone=(), geolocation=()', $response->headers->get('Permissions-Policy'));
    }

    public function test_admin_spa_has_csp(): void
    {
        $response = $this->get('/admin/');

        $response->assertOk();
        $csp = (string) $response->headers->get('Content-Security-Policy');
        $this->assertStringContainsString("default-src 'self'", $csp);
        $this->assertStringContainsString("frame-ancestors 'none'", $csp);
        $this->assertStringContainsString("style-src 'self' 'unsafe-inline'", $csp);
    }

    public function test_hsts_absent_outside_production(): void
    {
        $response = $this->getJson('/api/app/config');

        $this->assertNull($response->headers->get('Strict-Transport-Security'));
    }

    public function test_hsts_present_in_production_over_https(): void
    {
        app()->instance('env', 'production');

        $request = Request::create('https://example.test/api/app/config', 'GET');
        $request->server->set('HTTPS', 'on');

        $response = app(SecurityHeaders::class)->handle($request, fn ($r) => response('ok'));

        $this->assertSame('max-age=31536000; includeSubDomains', $response->headers->get('Strict-Transport-Security'));

        app()->instance('env', 'testing');
    }
}
