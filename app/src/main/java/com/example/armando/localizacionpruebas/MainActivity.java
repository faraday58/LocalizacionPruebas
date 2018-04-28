package com.example.armando.localizacionpruebas;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private static final long TIEMPO_MIN = 10 * 1000; // 10 segundos
    private static final long DISTANCIA_MIN = 5; // 5 metros
    private static final String[] A = {"n/d", "preciso", "impreciso"};
    private static final String[] P = {"n/d", "bajo", "medio", "alto"};
    private static final String[] E = {"fuera de servicio", "temporalmente no diponible"};

    private LocationManager manejador;
    private String proveedor;
    private TextView txtvMostrar;
    private int REQUEST_CODE_ACCESO_LOCALIZACION=1;
    Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtvMostrar = findViewById(R.id.txtvMostrar);
        manejador = (LocationManager) getSystemService(LOCATION_SERVICE);
        log("Proveedores de Localización: \n");
        muestraProveedores();
        Criteria criterio = new Criteria();
        criterio.setCostAllowed(false);
        criterio.setAltitudeRequired(false);
        criterio.setAccuracy(Criteria.ACCURACY_FINE);
        proveedor = manejador.getBestProvider(criterio, true);
        if (validarPermisos()) {

            Toast.makeText(MainActivity.this,"Permisos otorgados",Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(this,"Debes otorgar permisos para poder visualizar datos GPS",Toast.LENGTH_LONG).show();
        }

        log("Mejor proveedor: " + proveedor + "\n");
        log("Comenzamos con la última localización conocida: ");




    }

    private boolean validarPermisos() {

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        {
            return true;
        }
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            location = manejador.getLastKnownLocation(proveedor);
            muestraLocalizacion(location);

            return true;
        }
        if( shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) && shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION))
        {
                cargarDialogoRecomendacion();
        }
        else
        {
            requestPermissions(new String [] {Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE_ACCESO_LOCALIZACION);
        }


        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CODE_ACCESO_LOCALIZACION)
        {
            if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(MainActivity.this,"Los permisos fueron asignados",Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(MainActivity.this,"No se asignaron los permisos",Toast.LENGTH_LONG).show();
            }
        }
    }

    private void cargarDialogoRecomendacion() {
        AlertDialog.Builder dialogo = new AlertDialog.Builder(MainActivity.this);
        dialogo.setTitle("Permisos desactivados");
        dialogo.setMessage("Acepte los permisos para el funcionamiento correcto de la aplicaciòn");
        dialogo.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestPermissions(new String [] {Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE_ACCESO_LOCALIZACION);
            }
        });

        dialogo.show();
    }


    @Override
    protected void onResume() {
        super.onResume();

        //manejador.requestLocationUpdates(proveedor, TIEMPO_MIN, DISTANCIA_MIN, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        manejador.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
      log("Nueva Localización: ");
      muestraLocalizacion(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        log("Cambia estado proveedor:" + provider + ", estado=" + E[Math.max(0,status)]
        + ", extras=" + extras + "\n");
    }

    @Override
    public void onProviderEnabled(String provider) {
        log("Proveedor habilitado: " + provider + "\n" );
    }

    @Override
    public void onProviderDisabled(String provider) {
        log("Proveedor deshabilitado:"  + provider + "\n");
    }

    //Mostrando información
    private void log(String cadena){
        txtvMostrar.append(cadena + "\n");
    }

    private void muestraLocalizacion(Location localizacion)
    {
        if(localizacion == null)
        {
            log("Localización desconocida \n");
        }
        else {
            log(localizacion.toString() + "\n");
        }

    }

    private void muestraProveedores(){
        log("Proveedores de Localización: \n");
        List<String> proveedores = manejador.getAllProviders();
        for(String proveedor: proveedores)
        {
            muestraProveedor(proveedor);
        }
    }

    private void muestraProveedor(String proveedor) {
        LocationProvider info = manejador.getProvider(proveedor);
        log("LocationProvider[ " + "getName=" + info.getName()
        + ", isProviderEnabled="
        + manejador.isProviderEnabled(proveedor) + ", getAccuracy="
        + A[Math.max(0,info.getAccuracy())] + ", getPowerRequirement="
        + P[Math.max(0,info.getPowerRequirement())]
        + ", hasMonetaryCost=" + info.hasMonetaryCost()
        + ", requiresCell=" + info.requiresCell()
        + ", requiresNetwork=" + info.requiresNetwork()
        + ", requiresSatelite=" + info.requiresSatellite()
        + ", supportAltitude=" + info.supportsAltitude()
        + ", supportsBearing=" + info.supportsBearing()
        + ", supportsSpeed="  + info.supportsSpeed() + "\n"
        );

    }


}
