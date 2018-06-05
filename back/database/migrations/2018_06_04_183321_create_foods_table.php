<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;

class CreateFoodsTable extends Migration
{

    /**
     * Run the migrations.
     *
     * @return void
     */
    public function up()
    {
        Schema::create('foods', function (Blueprint $table) {
            $table->increments('id');
            $table->string('name', 255)->nullable(true);
            $table->integer('count')->unsigned();
            $table->integer('count_type')->unsigned();
            $table->date('production_date')->nullable(true);
            $table->date('expiration_date')->nullable(true);
            $table->string('note', 4095)->nullable(true);
            $table->string('img1', 1023)->nullable(true);
            $table->string('img2', 1023)->nullable(true);
            $table->string('img3', 1023)->nullable(true);
            $table->string('barcode', 64)->nullable(true);
            $table->integer('is_history', false, true)->default(false);
            $table->integer('history_id')->nullable(true);
            $table->timestamps();
            $table->softDeletes();
        });
    }

    /**
     * Reverse the migrations.
     *
     * @return void
     */
    public function down()
    {
        Schema::drop('foods');
    }
}
